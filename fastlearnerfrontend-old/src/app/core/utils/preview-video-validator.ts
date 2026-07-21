import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const previewVideoRequiredValidator: ValidatorFn = (
    group: AbstractControl
): ValidationErrors | null => {
    const previewName = group.get('previewPath')?.value;
    const youtubeUrl = group.get('youtubeUrl')?.value;

    const hasPreviewVideo =
        !!previewName && previewName.trim().length > 0;

    const hasYoutubeUrl =
        !!youtubeUrl && youtubeUrl.trim().length > 0;

    return hasPreviewVideo || hasYoutubeUrl
        ? null
        : { previewVideoRequired: true };
};
