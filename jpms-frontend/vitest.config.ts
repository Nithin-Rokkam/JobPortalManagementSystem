import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        environment: 'happy-dom',
        globals: true,
        include: ['src/**/*.spec.ts'],
        // Load Angular compiler before any test file runs
        setupFiles: ['src/test-setup.ts'],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html', 'lcov'],
            include: ['src/app/**/*.ts'],
            exclude: [
                'src/app/**/*.module.ts',
                'src/app/**/*.model.ts',
                'src/main.ts',
                'src/environments/**',
            ],
        },
    },
});
