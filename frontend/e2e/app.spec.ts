import { expect, test } from '@playwright/test';

import { registerMockApi, seedStoredSession } from './support/mock-api';

test('auth guard redirects unauthenticated users to login', async ({ page }) => {
  await registerMockApi(page);

  await page.goto('/admin');

  await expect(page).toHaveURL(/\/login\?returnUrl=%2Fadmin$/);
  await expect(page.getByRole('heading', { name: 'Log in to Lebhas' })).toBeVisible();
});

test('guest guard waits for backend validation before redirecting authenticated users', async ({ page }) => {
  const api = await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });
  await seedStoredSession(page, {
    activeWorkspaceId: api.ids.primaryWorkspaceId,
  });

  await page.goto('/login');

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByText(/session is active and ready/i)).toBeVisible();
  expect(api.counters.me).toBe(1);
});

test('login requires a backend-issued session', async ({ page }) => {
  const api = await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });

  await page.goto('/login');
  await page.getByTestId('login-identifier').fill('admin@example.com');
  await page.getByTestId('login-password').fill('CorrectPassword!1');
  await page.getByTestId('login-submit').click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByText(/session is active and ready/i)).toBeVisible();
  expect(api.counters.login).toBe(1);
});

test('expired access tokens restore through refresh before protected routes load', async ({ page }) => {
  const api = await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });
  await seedStoredSession(page, {
    accessTokenExpiresAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
    refreshTokenExpiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
    activeWorkspaceId: api.ids.primaryWorkspaceId,
  });

  await page.goto('/dashboard');

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByText(/session is active and ready/i)).toBeVisible();
  expect(api.counters.refresh).toBe(1);
});

test('logout clears the validated session and returns to login', async ({ page }) => {
  const api = await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });

  await page.goto('/login');
  await page.getByTestId('login-identifier').fill('admin@example.com');
  await page.getByTestId('login-password').fill('CorrectPassword!1');
  await page.getByTestId('login-submit').click();
  await expect(page).toHaveURL(/\/dashboard$/);

  await page.getByTestId('user-menu-trigger').click();
  await page.getByTestId('logout-button').click();

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByTestId(`remembered-profile-user-admin`)).toBeVisible();
  expect(api.counters.logout).toBe(1);
});

test('role guard blocks crew users from master routes', async ({ page }) => {
  await registerMockApi(page, {
    role: 'CREW',
    permissions: ['WORKSPACE_VIEW', 'ASSET_VIEW'],
    workspaceId: '11111111-1111-1111-1111-111111111111',
  });
  await seedStoredSession(page, {
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });

  await page.goto('/master');

  await expect(page).toHaveURL(/\/dashboard$/);
});

test('master users explicitly switch workspace before entering admin surfaces', async ({ page }) => {
  const api = await registerMockApi(page, {
    role: 'MASTER',
    workspaceId: null,
    activeWorkspaceId: null,
  });
  await seedStoredSession(page, {
    activeWorkspaceId: null,
  });

  await page.goto('/master');
  await page.getByTestId(`open-workspace-${api.ids.primaryWorkspaceId}`).click();

  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByText('Workspace status')).toBeVisible();
  await expect.poll(() =>
    page.evaluate(() => window.localStorage.getItem('creative_saas.active_workspace_id')),
  ).toBe(api.ids.primaryWorkspaceId);
});

test('asset filtering and uploads stay stable inside the selected workspace', async ({ page }) => {
  await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });
  await seedStoredSession(page, {
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });

  await page.goto('/admin/assets');
  await expect(page.getByText('lookbook-cover.jpg')).toBeVisible();
  await expect(page.getByText('brand-logo.png')).toBeVisible();

  await page.getByTestId('asset-search').fill('lookbook');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await expect(page.getByText('lookbook-cover.jpg')).toBeVisible();
  await expect(page.getByText('brand-logo.png')).toHaveCount(0);

  await page.getByTestId('open-asset-uploader').click();
  await page.getByTestId('asset-upload-file').setInputFiles({
    name: 'sample-upload.png',
    mimeType: 'image/png',
    buffer: Buffer.from('png'),
  });
  await page.getByTestId('asset-upload-category').selectOption('BRAND_LOGO');
  await page.getByTestId('asset-upload-submit').click();

  await expect(
    page.getByRole('button', { name: 'sample-upload.png', exact: true }),
  ).toBeVisible();
});

test('responsive layouts avoid horizontal overflow across target viewports', async ({ page }) => {
  await registerMockApi(page, {
    role: 'ADMIN',
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });
  await seedStoredSession(page, {
    activeWorkspaceId: '11111111-1111-1111-1111-111111111111',
  });

  const viewports = [
    { width: 1920, height: 1080 },
    { width: 1440, height: 900 },
    { width: 1366, height: 768 },
    { width: 1024, height: 768 },
    { width: 768, height: 1024 },
    { width: 430, height: 932 },
    { width: 390, height: 844 },
    { width: 360, height: 800 },
  ];

  for (const viewport of viewports) {
    await page.setViewportSize(viewport);
    await page.goto('/admin/assets');
    await expect(page.getByText('Asset library')).toBeVisible();

    const hasHorizontalOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth > window.innerWidth + 1,
    );
    expect(hasHorizontalOverflow).toBeFalsy();

    if (viewport.width < 1024) {
      await expect(page.getByLabel('Open sidebar')).toBeVisible();
    } else {
      await expect(page.getByPlaceholder('Search workspace')).toBeVisible();
    }
  }
});
