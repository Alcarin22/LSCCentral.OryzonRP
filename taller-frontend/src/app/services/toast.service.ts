import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  private counter = 0;

  show(message: string, type: ToastType = 'info'): void {
    const toast: Toast = {
      id: ++this.counter,
      message,
      type
    };

    this.toastsSubject.next([...this.toastsSubject.value, toast]);

    setTimeout(() => {
      this.remove(toast.id);
    }, 3200);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  warning(message: string): void {
    this.show(message, 'warning');
  }

  remove(id: number): void {
    this.toastsSubject.next(
      this.toastsSubject.value.filter(toast => toast.id !== id)
    );
  }
}