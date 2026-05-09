package com.lebhas.creativesaas.asset.application;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import com.lebhas.creativesaas.asset.domain.AssetFileType;
import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetFileValidationServiceTest {

    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder()
            .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2WZ/oAAAAASUVORK5CYII=");

    private final AssetFileValidationService validationService = new AssetFileValidationService();

    @Test
    void shouldAcceptSafeBrandLogoSvg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "brand-logo.svg",
                "image/svg+xml",
                """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10">
                  <rect width="10" height="10" fill="#000000"/>
                </svg>
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        AssetFileValidationService.ValidatedAssetFile validated = validationService.validate(file, AssetCategory.BRAND_LOGO);

        assertThat(validated.mimeType()).isEqualTo("image/svg+xml");
        assertThat(validated.fileType()).isEqualTo(AssetFileType.VECTOR_IMAGE);
        assertThat(validated.sanitizedFileName()).isEqualTo("brand-logo.svg");
    }

    @Test
    void shouldRejectScriptedSvg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "brand-logo.svg",
                "image/svg+xml",
                """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10">
                  <script>alert('xss')</script>
                </svg>
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThatThrownBy(() -> validationService.validate(file, AssetCategory.BRAND_LOGO))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.ASSET_FILE_CONTENT_INVALID);
    }

    @Test
    void shouldDetectServerSidePngSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "product-image.png",
                "application/octet-stream",
                ONE_PIXEL_PNG);

        AssetFileValidationService.ValidatedAssetFile validated = validationService.validate(file, AssetCategory.PRODUCT_IMAGE);

        assertThat(validated.mimeType()).isEqualTo("image/png");
        assertThat(validated.fileType()).isEqualTo(AssetFileType.IMAGE);
    }
}
