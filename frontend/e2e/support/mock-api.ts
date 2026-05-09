import { Page, Route } from '@playwright/test';

type UserRole = 'MASTER' | 'ADMIN' | 'CREW';

interface MockApiOptions {
  readonly role?: UserRole;
  readonly permissions?: readonly string[];
  readonly workspaceId?: string | null;
  readonly activeWorkspaceId?: string | null;
  readonly accessibleWorkspaceIds?: readonly string[];
}

interface SessionSeedOptions {
  readonly accessToken?: string;
  readonly refreshToken?: string;
  readonly accessTokenExpiresAt?: string;
  readonly refreshTokenExpiresAt?: string;
  readonly activeWorkspaceId?: string | null;
}

export interface MockApiHandle {
  readonly counters: {
    me: number;
    login: number;
    refresh: number;
    logout: number;
    upload: number;
  };
  readonly ids: {
    primaryWorkspaceId: string;
    secondaryWorkspaceId: string;
  };
}

const PRIMARY_WORKSPACE_ID = '11111111-1111-1111-1111-111111111111';
const SECONDARY_WORKSPACE_ID = '22222222-2222-2222-2222-222222222222';
const ACCESS_TOKEN_KEY = 'creative_saas.access_token';
const REFRESH_TOKEN_KEY = 'creative_saas.refresh_token';
const ACCESS_TOKEN_EXPIRES_AT_KEY = 'creative_saas.access_token_expires_at';
const REFRESH_TOKEN_EXPIRES_AT_KEY = 'creative_saas.refresh_token_expires_at';
const ACTIVE_WORKSPACE_ID_KEY = 'creative_saas.active_workspace_id';
const IMAGE_PREVIEW_URL =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="640" height="400"><rect width="640" height="400" fill="%230f766e"/><text x="32" y="72" font-size="40" fill="white">Lebhas</text></svg>';

export async function seedStoredSession(
  page: Page,
  options: SessionSeedOptions = {},
): Promise<void> {
  const accessTokenExpiresAt =
    options.accessTokenExpiresAt ?? new Date(Date.now() + 15 * 60 * 1000).toISOString();
  const refreshTokenExpiresAt =
    options.refreshTokenExpiresAt ?? new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();

  await page.addInitScript((seed) => {
    window.localStorage.setItem('creative_saas.access_token', seed.accessToken);
    window.localStorage.setItem('creative_saas.refresh_token', seed.refreshToken);
    window.localStorage.setItem('creative_saas.access_token_expires_at', seed.accessTokenExpiresAt);
    window.localStorage.setItem('creative_saas.refresh_token_expires_at', seed.refreshTokenExpiresAt);

    if (seed.activeWorkspaceId) {
      window.localStorage.setItem('creative_saas.active_workspace_id', seed.activeWorkspaceId);
    } else {
      window.localStorage.removeItem('creative_saas.active_workspace_id');
    }
  }, {
    accessToken: options.accessToken ?? 'seeded-access-token',
    refreshToken: options.refreshToken ?? 'seeded-refresh-token',
    accessTokenExpiresAt,
    refreshTokenExpiresAt,
    activeWorkspaceId: options.activeWorkspaceId ?? null,
  });
}

export async function registerMockApi(
  page: Page,
  options: MockApiOptions = {},
): Promise<MockApiHandle> {
  const role = options.role ?? 'ADMIN';
  const workspaceId =
    options.workspaceId === undefined
      ? role === 'MASTER'
        ? null
        : PRIMARY_WORKSPACE_ID
      : options.workspaceId;
  const accessibleWorkspaceIds =
    options.accessibleWorkspaceIds ??
    (role === 'MASTER'
      ? [PRIMARY_WORKSPACE_ID, SECONDARY_WORKSPACE_ID]
      : [workspaceId ?? PRIMARY_WORKSPACE_ID]);
  const permissions = [...(options.permissions ?? defaultPermissions(role))];
  const counters = {
    me: 0,
    login: 0,
    refresh: 0,
    logout: 0,
    upload: 0,
  };
  const user = buildUser(role, permissions, workspaceId);
  const workspaces = accessibleWorkspaceIds.map((id, index) =>
    buildWorkspace(id, index === 0 ? 'Lebhas Atelier' : 'Lebhas Studio East', role, permissions),
  );
  let assets = [
    buildAsset({
      id: 'asset-lookbook',
      originalFileName: 'lookbook-cover.jpg',
      assetCategory: 'PRODUCT_IMAGE',
      fileType: 'IMAGE',
      tags: ['lookbook', 'spring'],
      workspaceId: PRIMARY_WORKSPACE_ID,
    }),
    buildAsset({
      id: 'asset-logo',
      originalFileName: 'brand-logo.png',
      assetCategory: 'BRAND_LOGO',
      fileType: 'IMAGE',
      tags: ['identity'],
      workspaceId: PRIMARY_WORKSPACE_ID,
    }),
  ];
  const folders = [
    {
      id: 'folder-campaigns',
      workspaceId: PRIMARY_WORKSPACE_ID,
      name: 'Campaigns',
      parentFolderId: null,
      description: 'Campaign-ready media',
      createdBy: user.id,
      createdAt: isoNow(),
      updatedAt: isoNow(),
    },
  ];

  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const pathname = url.pathname;

    if (pathname === '/api/v1/auth/login' && request.method() === 'POST') {
      counters.login += 1;
      return json(route, 200, buildSession(role, permissions, workspaceId));
    }

    if (pathname === '/api/v1/auth/refresh' && request.method() === 'POST') {
      counters.refresh += 1;
      return json(
        route,
        200,
        buildSession(role, permissions, workspaceId, {
          accessToken: `refreshed-access-token-${counters.refresh}`,
        }),
      );
    }

    if (pathname === '/api/v1/auth/logout' && request.method() === 'POST') {
      counters.logout += 1;
      return json(route, 200, null, 'Logout completed');
    }

    if (pathname === '/api/v1/auth/me' && request.method() === 'GET') {
      counters.me += 1;
      return json(route, 200, user);
    }

    if (pathname === '/api/v1/workspaces/me' && request.method() === 'GET') {
      return json(route, 200, workspaces);
    }

    const workspaceMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)$/);
    if (workspaceMatch && request.method() === 'GET') {
      return json(route, 200, {
        ...findWorkspace(workspaces, workspaceMatch[1]),
        description: 'Workspace summary for automated browser validation.',
        industry: 'Fashion retail',
        currency: 'BDT',
        country: 'BD',
      });
    }

    const settingsMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/settings$/);
    if (settingsMatch && request.method() === 'GET') {
      return json(route, 200, {
        workspaceId: settingsMatch[1],
        allowCrewDownload: true,
        allowCrewPublish: false,
        defaultLanguage: 'ENGLISH',
        defaultTimezone: 'Asia/Dhaka',
        notificationPreferences: {
          crewInvites: true,
          workspaceUpdates: true,
          securityAlerts: true,
        },
        workspaceVisibility: 'PRIVATE',
        createdAt: isoNow(),
        updatedAt: isoNow(),
      });
    }

    const brandProfileMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/brand-profile$/);
    if (brandProfileMatch && request.method() === 'GET') {
      return json(route, 200, {
        id: `brand-${brandProfileMatch[1]}`,
        workspaceId: brandProfileMatch[1],
        brandName: 'Lebhas',
        businessType: 'Fashion retail',
        industry: 'Apparel',
        targetAudience: 'Modern apparel shoppers',
        brandVoice: 'Confident and refined',
        preferredCta: 'Shop now',
        primaryColor: '#0F766E',
        secondaryColor: '#F59E0B',
        website: 'https://example.com',
        facebookUrl: null,
        instagramUrl: null,
        linkedinUrl: null,
        tiktokUrl: null,
        description: 'Brand context for creative operations.',
        createdAt: isoNow(),
        updatedAt: isoNow(),
      });
    }

    const crewMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/crew(?:\/([^/]+))?$/);
    if (crewMatch && request.method() === 'GET') {
      return json(route, 200, [
        {
          userId: 'crew-1',
          workspaceId: crewMatch[1],
          firstName: 'Asha',
          lastName: 'Rahman',
          email: 'asha@example.com',
          phone: null,
          role: 'CREW',
          status: 'ACTIVE',
          permissions: ['WORKSPACE_VIEW', 'ASSET_VIEW', 'CREATIVE_GENERATE'],
          joinedAt: isoNow(),
          invitedByUserId: user.id,
          lastLoginAt: isoNow(),
          createdAt: isoNow(),
          updatedAt: isoNow(),
        },
      ]);
    }

    if (crewMatch && request.method() === 'POST') {
      return json(route, 200, {
        invitationToken: 'invite-token',
        workspaceId: crewMatch[1],
        email: 'new-crew@example.com',
        role: 'CREW',
        permissions: ['WORKSPACE_VIEW'],
        expiresAt: isoNow(),
        status: 'PENDING',
      });
    }

    if (crewMatch && (request.method() === 'PUT' || request.method() === 'DELETE')) {
      return json(route, 200, null);
    }

    const foldersMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/asset-folders(?:\/([^/]+))?$/);
    if (foldersMatch && request.method() === 'GET') {
      return json(route, 200, folders.filter((folder) => folder.workspaceId === foldersMatch[1]));
    }

    if (foldersMatch && request.method() === 'POST') {
      return json(route, 200, folders[0]);
    }

    if (foldersMatch && request.method() === 'PUT') {
      return json(route, 200, folders[0]);
    }

    if (foldersMatch && request.method() === 'DELETE') {
      return json(route, 200, null, 'Folder deleted');
    }

    const uploadMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/assets\/upload$/);
    if (uploadMatch && request.method() === 'POST') {
      counters.upload += 1;
      const uploadedAsset = buildAsset({
        id: `asset-upload-${counters.upload}`,
        originalFileName: 'sample-upload.png',
        assetCategory: 'BRAND_LOGO',
        fileType: 'IMAGE',
        tags: ['uploaded'],
        workspaceId: uploadMatch[1],
      });
      assets = [uploadedAsset, ...assets];
      return json(route, 200, uploadedAsset);
    }

    const previewMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/assets\/([^/]+)\/preview-url$/);
    if (previewMatch && request.method() === 'GET') {
      return json(route, 200, {
        url: IMAGE_PREVIEW_URL,
        expiresAt: isoNow(),
      });
    }

    const downloadMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/assets\/([^/]+)\/download-url$/);
    if (downloadMatch && request.method() === 'GET') {
      return json(route, 200, {
        url: 'https://example.com/download',
        expiresAt: isoNow(),
      });
    }

    const assetDetailMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/assets\/([^/]+)$/);
    if (assetDetailMatch && request.method() === 'GET') {
      const asset = assets.find((item) => item.id === assetDetailMatch[2]);
      if (!asset) {
        return error(route, 404, 'Asset not found');
      }
      return json(route, 200, asset);
    }

    if (assetDetailMatch && request.method() === 'PUT') {
      const asset = assets.find((item) => item.id === assetDetailMatch[2]) ?? assets[0];
      return json(route, 200, asset);
    }

    if (assetDetailMatch && request.method() === 'DELETE') {
      assets = assets.filter((item) => item.id !== assetDetailMatch[2]);
      return json(route, 200, null, 'Asset deleted');
    }

    const assetListMatch = pathname.match(/^\/api\/v1\/workspaces\/([^/]+)\/assets$/);
    if (assetListMatch && request.method() === 'GET') {
      if (!permissions.includes('ASSET_VIEW')) {
        return error(route, 403, 'Asset access denied');
      }

      let filteredAssets = assets.filter((asset) => asset.workspaceId === assetListMatch[1]);
      const search = url.searchParams.get('search')?.trim().toLowerCase();
      const category = url.searchParams.get('assetCategory');

      if (search) {
        filteredAssets = filteredAssets.filter((asset) => {
          const haystack = [
            asset.originalFileName,
            asset.storageKey,
            ...(asset.tags ?? []),
          ]
            .join(' ')
            .toLowerCase();
          return haystack.includes(search);
        });
      }

      if (category) {
        filteredAssets = filteredAssets.filter((asset) => asset.assetCategory === category);
      }

      return json(route, 200, {
        items: filteredAssets,
        totalItems: filteredAssets.length,
        totalPages: 1,
        page: 0,
        size: Math.max(filteredAssets.length, 1),
        first: true,
        last: true,
      });
    }

    return error(route, 404, `No mock available for ${pathname}`);
  });

  return {
    counters,
    ids: {
      primaryWorkspaceId: PRIMARY_WORKSPACE_ID,
      secondaryWorkspaceId: SECONDARY_WORKSPACE_ID,
    },
  };
}

function buildSession(
  role: UserRole,
  permissions: readonly string[],
  workspaceId: string | null,
  overrides?: { readonly accessToken?: string },
) {
  return {
    accessToken: overrides?.accessToken ?? 'mock-access-token',
    accessTokenExpiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    refreshToken: 'mock-refresh-token',
    refreshTokenExpiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    user: buildUser(role, permissions, workspaceId),
  };
}

function buildUser(role: UserRole, permissions: readonly string[], workspaceId: string | null) {
  return {
    id: `user-${role.toLowerCase()}`,
    firstName: role === 'MASTER' ? 'Master' : role === 'ADMIN' ? 'Admin' : 'Crew',
    lastName: 'User',
    email: `${role.toLowerCase()}@example.com`,
    phone: null,
    role,
    status: 'ACTIVE',
    emailVerified: true,
    lastLoginAt: isoNow(),
    workspaceId,
    createdAt: isoNow(),
    updatedAt: isoNow(),
    permissions,
  };
}

function buildWorkspace(
  id: string,
  name: string,
  role: UserRole,
  permissions: readonly string[],
) {
  return {
    id,
    name,
    slug: name.toLowerCase().replace(/\s+/g, '-'),
    logoUrl: null,
    status: 'ACTIVE',
    language: 'ENGLISH',
    timezone: 'Asia/Dhaka',
    ownerId: 'owner-1',
    currentUserRole: role,
    currentUserPermissions: permissions,
    createdAt: isoNow(),
    updatedAt: isoNow(),
  };
}

function buildAsset(options: {
  readonly id: string;
  readonly originalFileName: string;
  readonly assetCategory: string;
  readonly fileType: 'IMAGE' | 'VIDEO' | 'VECTOR_IMAGE';
  readonly tags: readonly string[];
  readonly workspaceId: string;
}) {
  const fileExtension = options.originalFileName.split('.').pop() ?? 'png';
  return {
    id: options.id,
    workspaceId: options.workspaceId,
    uploadedBy: 'user-admin',
    folderId: null,
    originalFileName: options.originalFileName,
    storedFileName: options.originalFileName,
    fileType: options.fileType,
    mimeType: fileExtension === 'png' ? 'image/png' : 'image/jpeg',
    fileExtension,
    fileSize: 150_000,
    storageProvider: 'LOCAL',
    storageBucket: null,
    storageKey: `assets/${options.id}/${options.originalFileName}`,
    publicUrl: IMAGE_PREVIEW_URL,
    previewUrl: IMAGE_PREVIEW_URL,
    thumbnailUrl: IMAGE_PREVIEW_URL,
    assetCategory: options.assetCategory,
    status: 'ACTIVE',
    width: 1200,
    height: 800,
    duration: null,
    tags: options.tags,
    metadata: {},
    createdAt: isoNow(),
    updatedAt: isoNow(),
  };
}

function defaultPermissions(role: UserRole): readonly string[] {
  if (role === 'MASTER') {
    return [
      'USER_VIEW',
      'USER_CREATE',
      'USER_UPDATE',
      'USER_STATUS_UPDATE',
      'WORKSPACE_CREATE',
      'WORKSPACE_VIEW',
      'WORKSPACE_UPDATE',
      'WORKSPACE_STATUS_UPDATE',
      'WORKSPACE_SETTINGS_VIEW',
      'WORKSPACE_SETTINGS_UPDATE',
      'BRAND_PROFILE_UPDATE',
      'CREW_VIEW',
      'CREW_INVITE',
      'CREW_UPDATE',
      'CREW_REMOVE',
      'ASSET_VIEW',
      'ASSET_UPLOAD',
      'ASSET_UPDATE',
      'ASSET_DELETE',
      'ASSET_FOLDER_MANAGE',
      'CREATIVE_GENERATE',
      'CREATIVE_EDIT',
      'CREATIVE_DOWNLOAD',
      'CREATIVE_SUBMIT',
      'SESSION_MANAGE',
    ];
  }

  if (role === 'ADMIN') {
    return [
      'WORKSPACE_VIEW',
      'WORKSPACE_UPDATE',
      'WORKSPACE_SETTINGS_VIEW',
      'WORKSPACE_SETTINGS_UPDATE',
      'BRAND_PROFILE_UPDATE',
      'CREW_VIEW',
      'CREW_INVITE',
      'CREW_UPDATE',
      'CREW_REMOVE',
      'ASSET_VIEW',
      'ASSET_UPLOAD',
      'ASSET_UPDATE',
      'ASSET_DELETE',
      'ASSET_FOLDER_MANAGE',
      'CREATIVE_GENERATE',
      'CREATIVE_EDIT',
      'CREATIVE_DOWNLOAD',
      'CREATIVE_SUBMIT',
      'SESSION_MANAGE',
    ];
  }

  return ['WORKSPACE_VIEW', 'ASSET_VIEW', 'CREATIVE_GENERATE'];
}

function findWorkspace(workspaces: readonly Record<string, unknown>[], workspaceId: string) {
  return workspaces.find((workspace) => workspace.id === workspaceId) ?? workspaces[0];
}

function isoNow(): string {
  return new Date().toISOString();
}

function envelope<T>(data: T, message = 'OK') {
  return {
    success: true,
    message,
    data,
    errors: [],
    timestamp: isoNow(),
  };
}

async function json(route: Route, status: number, data: unknown, message = 'OK') {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(envelope(data, message)),
  });
}

async function error(route: Route, status: number, message: string) {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify({
      success: false,
      message,
      data: null,
      errors: [{ code: 'ERROR', message }],
      timestamp: isoNow(),
    }),
  });
}
