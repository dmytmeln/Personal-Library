import {Injectable} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

@Injectable({
  providedIn: 'root'
})
export class LangService {

  constructor(private transloco: TranslocoService) {}

  setLang(lang: string) {
    this.transloco.setActiveLang(lang);
    localStorage.setItem('lang', lang);
  }

  loadSaved() {
    const saved = localStorage.getItem('lang') || 'en';
    this.transloco.setActiveLang(saved);
  }
}
