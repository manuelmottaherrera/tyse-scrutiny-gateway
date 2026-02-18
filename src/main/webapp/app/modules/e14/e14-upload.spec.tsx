import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { TranslatorContext } from 'react-jhipster';

import E14UploadPage from './e14-upload';
import e14Service from 'app/shared/services/e14.service';

// Mock the e14 service
jest.mock('app/shared/services/e14.service');
const mockE14Service = e14Service as jest.Mocked<typeof e14Service>;

describe('E14UploadPage', () => {
  beforeAll(() => {
    TranslatorContext.registerTranslations('es', {});
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('rendering', () => {
    it('should render upload title', () => {
      render(<E14UploadPage />);

      expect(screen.getByRole('heading', { name: /Subir Formulario E14/i })).toBeInTheDocument();
    });

    it('should render description text', () => {
      render(<E14UploadPage />);

      expect(screen.getByText(/Sube el formulario E14 escaneado/i)).toBeInTheDocument();
    });

    it('should render drop zone', () => {
      const { container } = render(<E14UploadPage />);

      expect(container.querySelector('.drop-zone')).toBeInTheDocument();
    });

    it('should render drop zone instructions', () => {
      render(<E14UploadPage />);

      expect(screen.getByText(/Arrastra el archivo PDF aquí/i)).toBeInTheDocument();
      expect(screen.getByText(/o haz clic para seleccionar/i)).toBeInTheDocument();
    });

    it('should render DIVIPOL key input', () => {
      render(<E14UploadPage />);

      expect(screen.getByLabelText(/Código DIVIPOL/i)).toBeInTheDocument();
    });

    it('should render table number input', () => {
      render(<E14UploadPage />);

      expect(screen.getByLabelText(/Número de Mesa/i)).toBeInTheDocument();
    });

    it('should render election process dropdown', () => {
      render(<E14UploadPage />);

      expect(screen.getByLabelText(/Proceso Electoral/i)).toBeInTheDocument();
    });

    it('should render upload button disabled by default', () => {
      render(<E14UploadPage />);

      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      expect(uploadButton).toBeDisabled();
    });
  });

  describe('file selection', () => {
    it('should accept PDF files via input', () => {
      const { container } = render(<E14UploadPage />);

      const fileInput = container.querySelector('input[type="file"]');
      expect(fileInput).toHaveAttribute('accept', 'application/pdf');
    });

    it('should show file preview when PDF is selected', async () => {
      const { container } = render(<E14UploadPage />);

      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');

      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });
    });

    it('should reject non-PDF file', async () => {
      const { container } = render(<E14UploadPage />);

      const file = new File(['dummy content'], 'test.jpg', { type: 'image/jpeg' });
      const fileInput = container.querySelector('input[type="file"]');

      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      // The file should not be shown (rejected)
      await waitFor(() => {
        expect(screen.queryByText('test.jpg')).not.toBeInTheDocument();
      });
    });

    it('should enable upload button when PDF is selected', async () => {
      const { container } = render(<E14UploadPage />);

      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');

      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
        expect(uploadButton).not.toBeDisabled();
      });
    });

    it('should show file size', async () => {
      const { container } = render(<E14UploadPage />);

      const content = new Array(1024).fill('a').join(''); // 1KB
      const file = new File([content], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');

      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText(/KB/i)).toBeInTheDocument();
      });
    });

    it('should show change file button when file is selected', async () => {
      const { container } = render(<E14UploadPage />);

      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');

      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('Cambiar archivo')).toBeInTheDocument();
      });
    });
  });

  describe('drag and drop', () => {
    it('should highlight drop zone on drag enter', () => {
      const { container } = render(<E14UploadPage />);

      const dropZone = container.querySelector('.drop-zone');
      fireEvent.dragEnter(dropZone);

      expect(dropZone).toHaveClass('drag-active');
    });

    it('should remove highlight on drag leave', () => {
      const { container } = render(<E14UploadPage />);

      const dropZone = container.querySelector('.drop-zone');
      fireEvent.dragEnter(dropZone);
      fireEvent.dragLeave(dropZone);

      expect(dropZone).not.toHaveClass('drag-active');
    });

    it('should accept dropped PDF files', async () => {
      const { container } = render(<E14UploadPage />);

      const file = new File(['dummy content'], 'dropped.pdf', { type: 'application/pdf' });
      const dropZone = container.querySelector('.drop-zone');

      const dataTransfer = {
        files: [file],
      };

      fireEvent.drop(dropZone, { dataTransfer });

      await waitFor(() => {
        expect(screen.getByText('dropped.pdf')).toBeInTheDocument();
      });
    });
  });

  describe('form inputs', () => {
    it('should update DIVIPOL key on change', () => {
      render(<E14UploadPage />);

      const input = screen.getByLabelText<HTMLInputElement>(/Código DIVIPOL/i);
      fireEvent.change(input, { target: { value: '31-001-10-01-001' } });

      expect(input.value).toBe('31-001-10-01-001');
    });

    it('should update table number on change', () => {
      render(<E14UploadPage />);

      const input = screen.getByLabelText<HTMLInputElement>(/Número de Mesa/i);
      fireEvent.change(input, { target: { value: '005' } });

      expect(input.value).toBe('005');
    });

    it('should update election process on change', () => {
      render(<E14UploadPage />);

      const select = screen.getByLabelText<HTMLSelectElement>(/Proceso Electoral/i);
      expect(select.value).toBe('1');
    });
  });

  describe('upload process', () => {
    it('should call e14Service.uploadE14 when upload button is clicked', async () => {
      mockE14Service.uploadE14.mockResolvedValue({ storageKey: 'test-key' });

      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      // Wait for file to be selected
      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click upload
      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      fireEvent.click(uploadButton);

      await waitFor(() => {
        expect(mockE14Service.uploadE14).toHaveBeenCalled();
      });
    });

    it('should show progress bar during upload', async () => {
      mockE14Service.uploadE14.mockImplementation((file, processId, divipol, table, onProgress) => {
        onProgress?.(50);
        return Promise.resolve({ storageKey: 'test-key' });
      });

      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click upload
      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      fireEvent.click(uploadButton);

      // Progress bar should appear
      await waitFor(() => {
        const progressBar = container.querySelector('.progress');
        expect(progressBar).toBeInTheDocument();
      });
    });

    it('should show success message on successful upload', async () => {
      mockE14Service.uploadE14.mockResolvedValue({ storageKey: 'test-storage-key' });

      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click upload
      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      fireEvent.click(uploadButton);

      // Success alert should appear
      await waitFor(() => {
        expect(container.querySelector('.alert-success')).toBeInTheDocument();
      });
    });

    it('should show storage key on successful upload', async () => {
      mockE14Service.uploadE14.mockResolvedValue({ storageKey: 'test-storage-key-12345' });

      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click upload
      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      fireEvent.click(uploadButton);

      // Storage key should be displayed
      await waitFor(() => {
        expect(screen.getByText(/test-storage-key-12345/)).toBeInTheDocument();
      });
    });

    it('should show error message on upload failure', async () => {
      mockE14Service.uploadE14.mockRejectedValue(new Error('Upload failed'));

      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click upload
      const uploadButton = screen.getByRole('button', { name: /Subir Formulario E14/i });
      fireEvent.click(uploadButton);

      // Error alert should appear
      await waitFor(() => {
        expect(container.querySelector('.alert-danger')).toBeInTheDocument();
      });
    });
  });

  describe('reset functionality', () => {
    it('should reset file selection when change file is clicked', async () => {
      const { container } = render(<E14UploadPage />);

      // Select a file
      const file = new File(['dummy content'], 'test.pdf', { type: 'application/pdf' });
      const fileInput = container.querySelector('input[type="file"]');
      Object.defineProperty(fileInput, 'files', { value: [file] });
      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });

      // Click change file
      const changeButton = screen.getByText('Cambiar archivo');
      fireEvent.click(changeButton);

      // Should go back to initial state
      await waitFor(() => {
        expect(screen.getByText(/Arrastra el archivo PDF aquí/i)).toBeInTheDocument();
      });
    });
  });
});
