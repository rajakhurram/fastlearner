import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import 'hammerjs';
import { AppModule } from './app/app.module';
import { handleClassLinkOAuthBootstrap } from './app/core/utils/classlink-oauth-bootstrap.util';

handleClassLinkOAuthBootstrap();

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch((err) => console.error(err));
