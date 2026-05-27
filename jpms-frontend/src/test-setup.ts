// Global test setup for vitest
// Required so Angular's JIT compiler is available when FormBuilder
// and other Angular injectables are used in isolation tests.
import '@angular/compiler';
