import { Component } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { MessageService } from 'src/app/core/services/message.service';
import { AdminService } from '../admin.service';
import { FormGroup, NgForm } from '@angular/forms';

interface AdminUserSetting {
  id: number;
  name: string;
  email: string;
  role: string;
  status: 'Active' | 'Inactive';
  lastLogin: string;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent {
  list: any[] = [];
  isTableLoading = false;
  isAddAdminModalVisible = false;
  adminEmail: string = '';
  link: string = 'https://fastlearner.ai/auth/sign-up';

  private _httpConstants: HttpConstants = new HttpConstants();
  totalElements: any;
  totalPages: any;

  constructor(
    private adminService: AdminService,
    private message: MessageService,
  ) {}

  ngOnInit(): void {
    this.getSettingsAdmins();
  }

  async getSettingsAdmins(): Promise<void> {
    this.isTableLoading = true;
    try {
      const res: any = await lastValueFrom(
        this.adminService.getSettingsAdmins(),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.list = res.data;
      } else {
        this.message.error(res.message);
      }
    } finally {
      this.isTableLoading = false;
    }
  }

  openAddAdminModal(): void {
    this.isAddAdminModalVisible = true;
    this.adminEmail = '';
    // this.link = '';
  }

  closeAddAdminModal(): void {
    this.isAddAdminModalVisible = false;
    this.adminEmail = '';
    // this.link = '';
  }

  async addAdmin(form: NgForm) {
    if (form.invalid) {
      return;
    }
    try {
      const res: any = await lastValueFrom(
        this.adminService.addAdmin({ email: this.adminEmail }),
      );
      if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
        this.message.success(res.message);
        this.closeAddAdminModal();
        this.getSettingsAdmins();
      } else {
        this.message.error(res.message || 'Unable to add admin.');
      }
    } catch (error: any) {
      this.message.error(
        error?.error?.message || error?.message || 'Unable to add admin.',
      );
    }
  }

  async deActivateAdmin(adminId: string) {
    const res: any = await lastValueFrom(
      this.adminService.deActivateAdmin(adminId),
    );
    if (res.status === this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE) {
      this.message.success(res.message);
      this.getSettingsAdmins();
    } else {
      this.message.error(res.message);
    }
  }
}
