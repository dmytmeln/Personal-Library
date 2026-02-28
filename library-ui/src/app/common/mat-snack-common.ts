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
    if (typeof err === 'string') {
      this.matSnack.open(err, 'OK', {duration: this.DURATION});
      return;
    }

    const errorData = err?.error;
    let message: string;

    if (errorData?.errors && Array.isArray(errorData.errors) && errorData.errors.length > 0) {
      message = errorData.errors.map((e: { field: string; message: string }) =>
        `${e.field}: ${e.message}`
      ).join(', ');
    } else {
      message = errorData?.message || err?.message || 'Щось пішло не так!';
    }

    this.matSnack.open(message, 'OK', {duration: this.DURATION});
  }

}
