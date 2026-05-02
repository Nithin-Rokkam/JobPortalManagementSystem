import { trigger, transition, style, animate, query, group } from '@angular/animations';

export const routeSlideAnimation = trigger('routeAnimations', [
    transition('* <=> *', [
        query(':enter', [style({ opacity: 0, transform: 'translateY(16px)' })], { optional: true }),
        group([
            query(':leave', [animate('200ms ease-in', style({ opacity: 0, transform: 'translateY(-8px)' }))], { optional: true }),
            query(':enter', [animate('300ms 100ms cubic-bezier(0,0,0.2,1)', style({ opacity: 1, transform: 'translateY(0)' }))], { optional: true }),
        ])
    ])
]);

export const fadeSlideUp = trigger('fadeSlideUp', [
    transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('350ms cubic-bezier(0,0,0.2,1)', style({ opacity: 1, transform: 'translateY(0)' }))
    ])
]);
