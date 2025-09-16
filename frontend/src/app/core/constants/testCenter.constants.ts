import { AngularEditorConfig } from '@kolkov/angular-editor';

export class TestCenterConst {
  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: 'auto',
    minHeight: '150px',
    maxHeight: 'auto',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    toolbarHiddenButtons: [
      [
        'strikeThrough',
        'subscript',
        'superscript',
        'justifyLeft',
        'justifyCenter',
        'justifyRight',
        'justifyFull',
        'indent',
        'outdent',
        'insertOrderedList',
        'insertUnorderedList',
        'heading',
        'fontSize',
        'textColor',
        'backgroundColor',
        'link',
        'unlink',
        'insertVideo',
        'insertHorizontalRule',
        'removeFormat',
        'toggleEditorMode',
        'undo',
        'redo',
        'fontName',
        'insertImage',
      ],
    ],
  };

  previewSrcs = {
    videoSrc: '../../../../../assets/images/add_video.svg',
    imageSrc: '../../../../assets/images/add_image.svg',
  };
}
