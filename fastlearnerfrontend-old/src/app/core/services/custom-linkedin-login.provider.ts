

//TODO

// declare const IN: any; 

// import { Injectable, Inject, InjectionToken } from '@angular/core';
// import {
//   BaseLoginProvider,
//   SocialUser,
//   LoginProvider,
// } from '@abacritt/angularx-social-login';
// import { environment } from 'src/environments/environment.development';

// export const LINKEDIN_CLIENT_ID = new InjectionToken<string>(environment.linkedInClientId);

// @Injectable()
// export class CustomLinkedInLoginProvider extends BaseLoginProvider implements LoginProvider {
//   public static readonly PROVIDER_ID: string = 'LINKEDIN';
  
//   constructor(@Inject(LINKEDIN_CLIENT_ID) private clientId: string) {
//     super();
//   }

//   initialize(): Promise<void> {
//     return new Promise((resolve, reject) => {
//       const linkedInSDKUrl = 'https://platform.linkedin.com/in.js';
//       const scriptElement = document.createElement('script');
//       scriptElement.src = linkedInSDKUrl;
//       scriptElement.text = `api_key: ${this.clientId}\nauthorize: true`;
//       scriptElement.async = true;
//       scriptElement.defer = true;
//       scriptElement.onload = () => {
//         resolve();
//       };
//       scriptElement.onerror = (error) => {
//         reject(error);
//       };
//       document.head.appendChild(scriptElement);
//     });
//   }

//   signIn(): Promise<SocialUser> {
//     return new Promise((resolve, reject) => {
//       if (typeof IN !== 'undefined') {
//         IN.User.authorize(() => {
//           console.log('Authorization successful');
//           IN.API.Raw('/people/~:(id,first-name,last-name,email-address,picture-url)').result((response: any) => {
//             console.log('User info retrieved:', response);
//             const user: SocialUser = new SocialUser();
//             user.id = response.id;
//             user.name = `${response.firstName} ${response.lastName}`;
//             user.email = response.emailAddress;
//             user.photoUrl = response.pictureUrl;
//             user.provider = CustomLinkedInLoginProvider.PROVIDER_ID;

//             resolve(user);
//           }).error((error: any) => {
//             console.error('Error retrieving user info:', error);
//             reject(error);
//           });
//         }, { scope: 'r_liteprofile r_emailaddress w_member_social' }); // Include scopes here
//       } else {
//         reject('LinkedIn SDK not loaded');
//       }
//     });
//   }

//   signOut(): Promise<void> {
//     return new Promise((resolve) => {
//       if (typeof IN !== 'undefined') {
//         IN.User.logout(() => {
//           resolve();
//         });
//       } else {
//         resolve();
//       }
//     });
//   }

//   getLoginStatus(): Promise<SocialUser> {
//     return new Promise((resolve, reject) => {
//       if (typeof IN !== 'undefined' && IN.User.isAuthorized()) {
//         IN.API.Raw('/people/~:(id,first-name,last-name,email-address,picture-url)').result((response: any) => {
//           const user: SocialUser = new SocialUser();
//           user.id = response.id;
//           user.name = `${response.firstName} ${response.lastName}`;
//           user.email = response.emailAddress;
//           user.photoUrl = response.pictureUrl;
//           user.provider = CustomLinkedInLoginProvider.PROVIDER_ID;

//           resolve(user);
//         }).error((error: any) => {
//           reject(error);
//         });
//       } else {
//         reject('User not logged in');
//       }
//     });
//   }
// }
