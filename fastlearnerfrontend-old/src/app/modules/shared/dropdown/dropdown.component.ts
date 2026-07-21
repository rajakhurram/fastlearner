import {
  Component,
  Input,
  Output,
  EventEmitter,
  HostBinding,
} from '@angular/core';

@Component({
  selector: 'app-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss'],
})
export class DropdownComponent {
  @Input() label: string = 'Select Option';
  @Input() placeholder: string = 'Choose an option';
  @Input() options: any[] = [];
  @Input() labelKey: string = 'name';
  @Input() valueKey: string = 'id';
  @Input() required: boolean = false;
  @Input() fullWidth: boolean = false;
  @Input() name: string = 'selectField';
  @Input() model: any;
  @Output() modelChange = new EventEmitter<any>();
  @HostBinding('style.max-width') hostMaxWidth: string = '50%';
  @HostBinding('style.flex') hostFlex: string = '0 0 50%';

  selectedValue: any = null;
  isTouched: boolean = false;

  ngOnInit() {
    this.selectedValue = this.model;

    if (this.fullWidth) {
      this.setFullWidth();
    } else {
      this.setHalfWidth();
    }
  }

  setFullWidth(): void {
    this.hostMaxWidth = '100%';
    this.hostFlex = '0 0 100%';
  }

  setHalfWidth(): void {
    this.hostMaxWidth = '50%';
    this.hostFlex = '0 0 50%';
  }

  onValueChange(value: any) {
    this.selectedValue = value;
    this.modelChange.emit(value);
  }

  onBlur() {
    this.isTouched = true;
  }
}
