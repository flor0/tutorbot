import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            // TODO: .env file for backend URL
            '/api': 'http://localhost:8080', // your Spring Boot port
        },
    },
})
