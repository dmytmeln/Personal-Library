import {MatSnackBar} from '@angular/material/snack-bar';

export class MatSnackCommon {

  private readonly DURATION = 5000;

  constructor(
    private matSnack: MatSnackBar,
  ) {
  }

  public showSuccess(message: string): void {
    this.matSnack.open(message, 'OK', {duration: this.DURATION});
  }

  public showError(err: any): void {
    const message = err?.error?.message || 'Щось пішло не так!';
    this.matSnack.open(message, 'OK', {duration: this.DURATION})
  }

}
