# JPMS — Angular Frontend (Joblix)

**Framework:** Angular 19+ | **Port (dev):** `4200` | **API Gateway:** `http://localhost:9090`

The Joblix frontend is a feature-rich Angular application with a "Celestial" design system — glassmorphism surfaces, dark/light theme toggle, and smooth animations. It is organized into lazy-loaded feature modules for Auth, Job Seeker, Recruiter, and Admin roles, with a shared component library, reactive state management via RxJS `BehaviorSubject`, and a JWT interceptor for automatic token attachment and refresh.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [App Bootstrap](#app-bootstrap)
4. [Routing — app-routing-module.ts](#routing--app-routing-modulets)
5. [Core Module](#core-module)
6. [Shared Module](#shared-module)
7. [Layout Module](#layout-module)
8. [Feature Modules](#feature-modules)
9. [Services](#services)
10. [Guards](#guards)
11. [Interceptors](#interceptors)
12. [Models](#models)
13. [Pipes and Directives](#pipes-and-directives)
14. [Environment Configuration](#environment-configuration)
15. [Running the Application](#running-the-application)

---

## Technology Stack

| Concern | Technology |
|---|---|
| Framework | Angular 19+ |
| Language | TypeScript |
| Styling | Custom SCSS (Celestial Design System) |
| HTTP | Angular `HttpClient` + Interceptors |
| State Management | RxJS `BehaviorSubject` (no NgRx) |
| Forms | Reactive Forms (`FormBuilder`, `FormGroup`) |
| Routing | Angular Router with lazy loading |
| Auth Guards | `CanActivateFn` guards |
| Testing | Vitest |
| Build | Angular CLI |

---

## Project Structure

```
jpms-frontend/src/app/
├── app.ts                          ← Root component
├── app.html                        ← Root template
├── app-module.ts                   ← Root NgModule
├── app-routing-module.ts           ← Top-level route definitions
│
├── core/                           ← Singleton services, guards, interceptors
│   ├── guards/
│   │   ├── auth.guard.ts           ← Redirects to login if not authenticated
│   │   └── role.guard.ts           ← Redirects if role doesn't match route
│   ├── interceptors/
│   │   └── auth.interceptor.ts     ← Attaches Bearer token + handles 401 refresh
│   ├── services/
│   │   ├── auth.service.ts         ← Token storage, user state, logout
│   │   ├── loader.service.ts       ← Global loading spinner state
│   │   └── toast.service.ts        ← Toast notification service
│   └── core.module.ts
│
├── shared/                         ← Reusable UI components, models, pipes
│   ├── components/
│   │   ├── badge/                  ← Status badge (APPLIED, SHORTLISTED, etc.)
│   │   ├── confirm-modal/          ← Reusable confirmation dialog
│   │   ├── empty-state/            ← Empty list placeholder
│   │   ├── job-card/               ← Job listing card
│   │   ├── loader/                 ← Global loading spinner
│   │   ├── page-header/            ← Page title + breadcrumb component
│   │   ├── stat-card/              ← Dashboard statistics card
│   │   └── unauthorized/           ← 403 page component
│   ├── directives/
│   │   └── role-hide.directive.ts  ← *appRoleHide structural directive
│   ├── models/
│   │   ├── application.model.ts
│   │   ├── job.model.ts
│   │   ├── platform-report.model.ts
│   │   └── user.model.ts
│   ├── pipes/
│   │   ├── capitalize.pipe.ts
│   │   ├── salary-range.pipe.ts
│   │   └── time-ago.pipe.ts
│   ├── animations/
│   │   └── route-animations.ts     ← Angular animation definitions
│   └── shared.module.ts
│
├── layout/                         ← Shell layout components
│   ├── header/                     ← Top navigation bar
│   ├── footer/                     ← Footer component
│   ├── sidebar/                    ← Side navigation
│   ├── shell/                      ← Layout wrapper (header + router-outlet + footer)
│   └── layout.module.ts
│
└── features/                       ← Lazy-loaded feature modules
    ├── auth/                       ← Login, Register, Forgot/Reset Password, OTP Verify
    ├── home/                       ← Public landing page
    ├── seeker/                     ← Job Seeker features
    ├── recruiter/                  ← Recruiter features
    └── admin/                      ← Admin features
```

---

## App Bootstrap

### app-module.ts
The root `NgModule` imports `BrowserModule`, `BrowserAnimationsModule`, `HttpClientModule`, and the `AppRoutingModule`. It declares the root `AppComponent` and provides the `AuthInterceptor` via `HTTP_INTERCEPTORS`.

### app.ts (AppComponent)
The root component. Hosts the `<router-outlet>` where all feature views are rendered.

---

## Routing — app-routing-module.ts

All feature modules are **lazy-loaded** using `loadChildren`. Guards are applied at the parent route level.

```typescript
const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  // Public
  { path: 'auth', loadChildren: () => import('./features/auth/auth.module') },
  { path: 'home', loadChildren: () => import('./features/home/home.module') },

  // Job Seeker — requires auth + JOB_SEEKER role
  {
    path: 'seeker',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['JOB_SEEKER'] },
    loadChildren: () => import('./features/seeker/seeker.module')
  },

  // Recruiter — requires auth + RECRUITER role
  {
    path: 'recruiter',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['RECRUITER'] },
    loadChildren: () => import('./features/recruiter/recruiter.module')
  },

  // Admin — requires auth + ADMIN role
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/admin/admin.module')
  },

  { path: 'unauthorized', loadComponent: () => import('./shared/components/unauthorized/...') },
  { path: '**', redirectTo: 'auth/login' }
];
```

---

## Core Module

Loaded once at app startup. Contains singleton services and infrastructure.

### AuthService (`core/services/auth.service.ts`)

The central state management service for authentication.

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY    = 'joblix_access_token';
    private readonly REFRESH_KEY  = 'joblix_refresh_token';
    private readonly USER_KEY     = 'joblix_user';

    private currentUserSubject: BehaviorSubject<any>;
    public currentUser$: Observable<any>;   // All components subscribe to this
}
```

**Key Methods:**

| Method | Description |
|---|---|
| `saveTokens(access, refresh)` | Stores both tokens in `localStorage` |
| `getAccessToken()` | Returns access token from `localStorage` |
| `getRefreshToken()` | Returns refresh token from `localStorage` |
| `saveUser(user)` | Stores user object in `localStorage` and emits to `currentUser$` |
| `getUser()` | Returns current user from `BehaviorSubject` value |
| `getRole()` | Returns `user.role` from current user |
| `isLoggedIn()` | Returns `true` if access token exists |
| `logout()` | Clears all storage, emits `null`, navigates to `/auth/login` |
| `redirectByRole()` | Navigates to role-specific dashboard after login |

**State flow:** On app init, the constructor reads `localStorage` and initializes `BehaviorSubject` with the stored user. All components (especially `HeaderComponent`) subscribe to `currentUser$` to reactively update the UI.

### LoaderService (`core/services/loader.service.ts`)
Exposes a `BehaviorSubject<boolean>` for showing/hiding the global loading spinner. The `LoaderComponent` subscribes to it.

### ToastService (`core/services/toast.service.ts`)
Provides `success()`, `error()`, `info()`, and `warning()` methods for displaying toast notifications.

---

## Shared Module

Declares and exports reusable components, pipes, and directives used across all feature modules.

### Components

#### BadgeComponent
Displays color-coded status badges for application statuses (`APPLIED`, `UNDER_REVIEW`, `SHORTLISTED`, `SELECTED`, `REJECTED`). Accepts a `status` input and applies the corresponding CSS class.

#### ConfirmModalComponent
A reusable confirmation dialog. Accepts `title`, `message`, and `confirmText` inputs. Emits `confirmed` and `cancelled` events.

#### JobCardComponent
Displays a job listing card with title, company, location, job type, salary, and an action button. Used in browse-jobs and dashboard views.

#### LoaderComponent
Full-screen loading overlay. Subscribes to `LoaderService.loading$` and shows/hides based on the value.

#### PageHeaderComponent
Renders a page title with optional subtitle and breadcrumb navigation. Accepts `title`, `subtitle`, and `breadcrumbs` inputs.

#### StatCardComponent
Dashboard statistics card. Accepts `label`, `value`, and `icon` inputs.

#### EmptyStateComponent
Placeholder shown when a list is empty. Accepts `message` and `icon` inputs.

#### UnauthorizedComponent
Standalone component shown at `/unauthorized` when a user tries to access a route they don't have permission for.

### Directives

#### RoleHideDirective (`*appRoleHide`)
Structural directive that removes an element from the DOM if the current user's role matches any of the provided roles.

```html
<button *appRoleHide="['ADMIN']">Not visible to admins</button>
```

### Pipes

#### CapitalizePipe
Transforms `"hello world"` → `"Hello World"`.

#### TimeAgoPipe
Transforms a timestamp into a relative string: `"2 hours ago"`, `"3 days ago"`.

#### SalaryRangePipe
Formats salary values: `40000` → `"₹40K"`.

---

## Layout Module

### HeaderComponent (`layout/header/`)
The top navigation bar. Subscribes to `AuthService.currentUser$` for reactive updates.

**Features:**
- Brand logo with role-based home route.
- Navigation links (Explore Jobs, Post Job, My Applications) shown based on role.
- Theme toggle (dark/light) — persists to `localStorage`.
- User avatar dropdown: shows profile picture if available, falls back to initials.
- Dropdown menu: View Profile, Settings, Sign Out.

**Key properties:**
```typescript
user: any;           // Current user from AuthService.currentUser$
showDropdown: boolean;
isDark: boolean;     // Theme state

get logoRoute(): string   // Returns role-specific home route
get profileRoute(): string // Returns role-specific profile route
```

### ShellComponent (`layout/shell/`)
Wraps the authenticated layout: `<app-header>` + `<router-outlet>` + `<app-footer>`. Used as the parent component for all authenticated routes.

### SidebarComponent (`layout/sidebar/`)
Role-aware side navigation. Shows different links based on the current user's role.

### FooterComponent (`layout/footer/`)
Simple footer with branding and links.

---

## Feature Modules

### Auth Module (`features/auth/`)

**Routes:**
```
/auth/login                  → LoginComponent
/auth/register               → RegisterComponent
/auth/forgot-password        → ForgotPasswordComponent
/auth/reset-password         → ResetPasswordComponent
/auth/verify-registration    → VerifyRegistrationComponent
```

**AuthApiService** (`features/auth/services/auth-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `register(data)` | POST | `/api/auth/register` | Register new user |
| `login(data)` | POST | `/api/auth/login` | Login, returns tokens |
| `refresh(token)` | POST | `/api/auth/refresh` | Rotate tokens |
| `logout(token)` | POST | `/api/auth/logout` | Invalidate refresh token |
| `getProfile()` | GET | `/api/auth/profile` | Get logged-in user profile |
| `uploadProfilePicture(file)` | PUT | `/api/auth/profile/picture` | Upload profile picture |
| `uploadResume(file)` | PUT | `/api/auth/profile/resume` | Upload resume |
| `updateCompanyName(name)` | PUT | `/api/auth/profile/company` | Update company name |
| `verifyRegistration(email, otp)` | POST | `/api/auth/verify-registration` | Verify email OTP |
| `resendRegistrationOtp(email)` | POST | `/api/auth/resend-registration-otp` | Resend OTP |
| `forgotPassword(email)` | POST | `/api/auth/forgot-password` | Request password reset OTP |
| `resetPassword(email, otp, newPassword)` | POST | `/api/auth/reset-password` | Reset password |

**LoginComponent** — Reactive form with email/password validation. On success, calls `auth.saveTokens()` and `auth.saveUser()`, then `auth.redirectByRole()`. Handles `EMAIL_NOT_VERIFIED` error by redirecting to OTP verification.

**RegisterComponent** — Reactive form with role selection (JOB_SEEKER / RECRUITER). Shows company name field conditionally for RECRUITER. On success, redirects to OTP verification.

---

### Seeker Module (`features/seeker/`)

**Routes:**
```
/seeker/dashboard     → SeekerDashboardComponent
/seeker/browse        → BrowseJobsComponent
/seeker/jobs/:id      → JobDetailComponent
/seeker/applications  → MyApplicationsComponent
/seeker/profile       → SeekerProfileComponent
```

**JobsApiService** (`features/seeker/services/jobs-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `getAll(page, size)` | GET | `/api/jobs?page=&size=` | Paginated job list |
| `getById(id)` | GET | `/api/jobs/{id}` | Single job detail |
| `search(filters)` | GET | `/api/jobs/search` | Search with filters |

**ApplicationsApiService** (`features/seeker/services/applications-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `apply(data)` | POST | `/api/applications` | Apply for a job (multipart) |
| `getMyApplications()` | GET | `/api/applications/my-applications` | Own applications |
| `getApplicationById(id)` | GET | `/api/applications/{id}` | Single application |

---

### Recruiter Module (`features/recruiter/`)

**Routes:**
```
/recruiter/dashboard          → RecruiterDashboardComponent
/recruiter/post-job           → PostJobComponent
/recruiter/my-jobs            → MyJobsComponent
/recruiter/jobs/:id/edit      → EditJobComponent
/recruiter/jobs/:id/applicants → ViewApplicantsComponent
/recruiter/all-applicants     → AllApplicantsComponent
/recruiter/profile            → RecruiterProfileComponent
```

**RecruiterJobsApiService** (`features/recruiter/services/recruiter-jobs-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `getMyJobs(page, size)` | GET | `/api/jobs/my-jobs` | Recruiter's own jobs |
| `postJob(data)` | POST | `/api/jobs` | Create new job |
| `updateJob(id, data)` | PUT | `/api/jobs/{id}` | Update job |
| `deleteJob(id)` | DELETE | `/api/jobs/{id}` | Soft-delete job |

**RecruiterAppsApiService** (`features/recruiter/services/recruiter-apps-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `getApplicantsForJob(jobId)` | GET | `/api/applications/job/{jobId}` | All applicants for a job |
| `updateApplicationStatus(id, status)` | PATCH | `/api/applications/{id}/status` | Update application status |
| `deleteApplication(id)` | DELETE | `/api/applications/{id}` | Delete application |

**RecruiterDashboardComponent** — Shows greeting, stats (total/active/draft jobs), CTA card, and recent postings. On init, fetches full profile from `AuthApiService.getProfile()` and syncs `profilePictureUrl` back into `AuthService` so the navbar avatar stays current.

---

### Admin Module (`features/admin/`)

**Routes:**
```
/admin/dashboard      → AdminDashboardComponent
/admin/users          → UserManagementComponent
/admin/jobs           → JobManagementComponent
/admin/reports        → PlatformReportComponent
/admin/audit-logs     → AuditLogsComponent
/admin/profile        → AdminProfileComponent
```

**AdminApiService** (`features/admin/services/admin-api.service.ts`):

| Method | HTTP | Endpoint | Description |
|---|---|---|---|
| `getUsers()` | GET | `/api/admin/users` | All users |
| `deleteUser(id)` | DELETE | `/api/admin/users/{id}` | Delete user |
| `banUser(id)` | PUT | `/api/admin/users/{id}/ban` | Ban user |
| `unbanUser(id)` | PUT | `/api/admin/users/{id}/unban` | Unban user |
| `getJobs()` | GET | `/api/admin/jobs` | All jobs |
| `deleteJob(id)` | DELETE | `/api/admin/jobs/{id}` | Delete job |
| `getReport()` | GET | `/api/admin/reports` | Platform report |
| `getAuditLogs()` | GET | `/api/admin/audit-logs` | Audit logs |

---

## Services

### AuthService
See [Core Module → AuthService](#authservice-coreservicesauthservicets) above.

### AuthApiService
See [Auth Module → AuthApiService](#authmodule-featuresauth) above.

---

## Guards

### authGuard (`core/guards/auth.guard.ts`)
```typescript
export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isLoggedIn()) return true;
    router.navigate(['/auth/login']);
    return false;
};
```
Redirects to `/auth/login` if no access token is present in `localStorage`.

### roleGuard (`core/guards/role.guard.ts`)
```typescript
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
    const allowedRoles: string[] = route.data['roles'] ?? [];
    const userRole = inject(AuthService).getRole();
    if (userRole && allowedRoles.includes(userRole)) return true;
    inject(Router).navigate(['/unauthorized']);
    return false;
};
```
Reads `data.roles` from the route definition and compares with the current user's role. Redirects to `/unauthorized` if the role doesn't match.

---

## Interceptors

### AuthInterceptor (`core/interceptors/auth.interceptor.ts`)

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = inject(AuthService).getAccessToken();
    const authReq = token
        ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : req;

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !req.url.includes('/refresh')) {
                // Attempt token refresh
                return inject(AuthApiService).refresh(refreshToken).pipe(
                    switchMap(res => {
                        auth.saveTokens(res.accessToken, res.refreshToken);
                        return next(req.clone({ setHeaders: { Authorization: `Bearer ${res.accessToken}` } }));
                    }),
                    catchError(() => { auth.logout(); return throwError(() => error); })
                );
            }
            return throwError(() => error);
        })
    );
};
```

**Behavior:**
1. Attaches `Authorization: Bearer <token>` to every outgoing HTTP request.
2. On `401 Unauthorized` (and not a refresh request): attempts to refresh the token.
3. On successful refresh: retries the original request with the new token.
4. On failed refresh: calls `auth.logout()` and redirects to login.

---

## Models

### user.model.ts
```typescript
export type UserRole   = 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'BANNED';

export interface User {
    id: number; name: string; email: string; phone?: string;
    role: UserRole; status: UserStatus;
    profilePictureUrl?: string; resumeUrl?: string;
    createdAt: string; updatedAt: string;
}
```

### job.model.ts
```typescript
export type JobType   = 'FULL_TIME' | 'PART_TIME' | 'REMOTE' | 'CONTRACT';
export type JobStatus = 'ACTIVE' | 'CLOSED' | 'DRAFT' | 'DELETED';

export interface Job {
    id: number; title: string; companyName: string; location: string;
    description: string; jobType: JobType; status: JobStatus;
    salary: number; experienceYears: number;
    applicationDeadline: string; postedBy: number;
    createdAt: string; updatedAt: string;
}

export interface PagedResponse<T> {
    content: T[]; totalPages: number; totalElements: number;
    currentPage: number; size: number;
}
```

### application.model.ts
```typescript
export type ApplicationStatus = 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'SELECTED' | 'REJECTED';

export interface Application {
    id: number; jobId: number; userId: number;
    resumeUrl: string; coverLetter?: string;
    status: ApplicationStatus; appliedAt: string;
}
```

---

## Pipes and Directives

### CapitalizePipe
`{{ 'hello world' | capitalize }}` → `"Hello World"`

### TimeAgoPipe
`{{ application.appliedAt | timeAgo }}` → `"3 hours ago"`

### SalaryRangePipe
`{{ 40000 | salaryRange }}` → `"₹40K"`

### RoleHideDirective
```html
<div *appRoleHide="['ADMIN', 'RECRUITER']">Only visible to job seekers</div>
```

---

## Environment Configuration

### `environments/environment.ts`
```typescript
export const environment = {
    production: false,
    apiUrl: 'http://localhost:9090'   // API Gateway base URL
};
```

### `environments/environment.prod.ts`
```typescript
export const environment = {
    production: true,
    apiUrl: 'https://your-production-gateway.com'
};
```

All API service classes use `environment.apiUrl` as the base URL, making it trivial to switch between environments.

---

## Running the Application

### Prerequisites
- Node.js 20+
- Angular CLI 19+

### Development
```bash
cd jpms-frontend
npm install
npm run dev        # or: ng serve
```
App runs at `http://localhost:4200`. Ensure the backend services are running at `http://localhost:9090`.

### Production Build
```bash
npm run build      # or: ng build --configuration production
```
Output goes to `dist/jpms-frontend/`.

### Running Tests
```bash
npm run test       # Vitest in watch mode
npm run test:run   # Vitest single run
```
