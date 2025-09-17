import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { buttonConfig } from 'src/app/core/models/button.model-config';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent {
  private defaultConfig: buttonConfig = {
    type: 'default',
    size: 'small',
    iconType: '',
    backgroundColor: '#fe4a55',
    color: 'white',
    borderColor: 'none',
    border: 'none',
    outline: 'none',
    borderRadius: '4px',
    height: '45px',
    paddingTop: '10px',
    paddingRight: '20px',
    paddingBottom: '10px',
    paddingLeft: '20px',
    fontSize: '14px',
    hoverConfig: null,
    fontWeight: 'normal'

  };

  private _config: buttonConfig = { ...this.defaultConfig };

  @Input()
  set config(value: buttonConfig) {
    this._config = { ...this.defaultConfig, ...value };
  }

  get config(): buttonConfig {
    return this._config;
  }
  @Input() buttonText: string;
  @Input() disabled = false;
  input?: string = '';

  @Output() buttonCallBack = new EventEmitter<any>();

  ngOnInit() {}

  callBackFn(event) {
    this.buttonCallBack.emit(event);
  }
}
