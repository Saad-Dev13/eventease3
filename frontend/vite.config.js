import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';

export default defineConfig({
  plugins: [react()],
  preview: {
    host: true,   // This will bind to 0.0.0.0 in Docker
    port: 5173
  }
});


