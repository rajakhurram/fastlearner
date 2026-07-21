import {
  Component,
  Input,
  Output,
  EventEmitter,
  HostBinding,
  OnChanges,
  OnDestroy,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
})
export class InputComponent implements OnChanges, OnDestroy {
  @Input() name: string = '';
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() accept: string = 'video/mp4, video/webm';
  @Input() idForAccesibility: string = 'video';
  @Input() fileName: string = '';
  @Input() btnTxt: string = 'Upload';
  @Input() minLength: number | null = null;
  @Input() maxLength: number | null = null;
  @Input() required: boolean = false;
  @Input() fullWidth: boolean = false;
  @Input() model: any = null;
  @Input() value: any = null;
  @Input() staticVal: string = '';
  @Input() isInputGroup: boolean = false;
  @Input() isTooltip: boolean = false;
  @Input() isFileInput: boolean = false;
  @Output() modelChange = new EventEmitter<any>();
  @Output() validationError = new EventEmitter<string | null>();
  @Output() uploadFile = new EventEmitter<any>();
  timeOut: number;
  inputName: any = null;
  isInputTouched: boolean = false;
  errorMessage: string | null = null;
  @HostBinding('style.max-width') hostMaxWidth: string = '50%';
  @HostBinding('style.flex') hostFlex: string = '0 0 50%';

  ngOnInit() {
    if (this.fullWidth) {
      this.setFullWidth();
    } else {
      this.setHalfWidth();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value']) {
      const curr = changes['value'].currentValue;
      this.model = curr;
    }
  }

  ngOnDestroy(): void {
    clearTimeout(this.timeOut);
  }

  setFullWidth(): void {
    this.hostMaxWidth = '100%';
    this.hostFlex = '0 0 100%';
  }

  setHalfWidth(): void {
    this.hostMaxWidth = '50%';
    this.hostFlex = '0 0 50%';
  }

  onChange(event: any) {
    this.modelChange.emit(this.model);
    this.isInputTouched = true;
    this.validate(this.model);
  }

  onBlur() {
    this.inputName = this.model?.trim() == '' ? null : this.model;
    this.isInputTouched = this.model == null || this.model?.trim() == '';
    this.validate(this.model);
  }

  validate(value: any) {
    if (this.required && (!value || value.trim() === '')) {
      this.errorMessage = `${this.label} is required.`;
    } else if (this.minLength && value.length < this.minLength) {
      this.errorMessage = `${this.label} must be at least ${this.minLength} characters long.`;
    } else if (this.maxLength && value.length > this.maxLength) {
      this.errorMessage = `${this.label} cannot be longer than ${this.maxLength} characters.`;
    } else {
      this.errorMessage = null;
    }
    this.validationError.emit(this.errorMessage);
  }

  copyUrlToClipboard() {
    const fullUrl = this.staticVal + this.model;
    navigator.clipboard
      .writeText(fullUrl)
      .then(() => {
        alert('URL copied to clipboard!');
      })
      .catch((err) => {
        console.error('Failed to copy: ', err);
      });
  }

  handleFileUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.fileName = file.name;

      if (!file) return;
      this.isInputTouched = false;

      const fileContent = {
        url: URL.createObjectURL(file),
        name: this.fileName,
      };
      this.timeOut = window.setTimeout(() => {
        this.uploadFile.emit(fileContent);
      }, 1000);
    }
  }

  markFileAsUpload() {
    this.isInputTouched = this.fileName == null || this.fileName.trim() == '';
    if (this.required && this.isInputTouched) {
      this.errorMessage = 'File must be required';
    }
  }
}
