import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '../services/auth.service';

import {TranslocoDirective, TranslocoPipe, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    RouterLink,
    TranslocoDirective,
    TranslocoPipe,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {

  registerForm: FormGroup;
  errorMessage: string = '';
  hidePassword: boolean = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private translocoService: TranslocoService
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          const {email, password} = this.registerForm.value;
          this.authService.login({email, password}).subscribe({
            next: () => {
              this.router.navigate(['/']);
            },
            error: (error) => {
              this.errorMessage = this.translocoService.translate('auth.register.loginFailed');
              console.error('Auto-login error:', error);
              this.router.navigate(['/login']);
            }
          });
        },
        error: (error) => {
          this.errorMessage = error.error?.message || this.translocoService.translate('auth.register.error');
          console.error('Registration error:', error);
        }
      });
    }
  }

}
