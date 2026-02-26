import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';

@Injectable()
export class LangInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler) {

    const lang = localStorage.getItem('lang') || 'en';

    const clone = req.clone({
      setHeaders: {
        'Accept-Language': lang
      }
    });

    return next.handle(clone);
  }
}
