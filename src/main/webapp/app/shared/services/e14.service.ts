import axios from 'axios';

// Tipos para Storage (presigned URLs)
export interface PresignedUrlRequest {
  fileName: string;
  contentType: string;
}

export interface PresignedUrlResponse {
  uploadUrl: string;
  objectKey: string;
  bucket: string;
  error?: string;
}

export interface PresignedDownloadResponse {
  downloadUrl: string;
  objectKey: string;
}

// Tipos para notificación de upload
export interface E14NotifyRequest {
  fileName: string;
  storageKey: string;
  electionProcessId: number;
  divipolKey?: string;
  tableNumber?: string;
}

export interface E14NotifyResponse {
  message: string;
  storageKey: string;
  topic: string;
}

// Tipos para E14 Form
export interface E14Form {
  id: string;
  divipolKey: string;
  depCode: string;
  munCode: string;
  zoneCode: string;
  tableCode: string;
  totalVoters: number;
  totalBallotBoxVotes: number;
  processedAt: string;
  status: E14Status;
}

export type E14Status = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';

// URLs de la API
const STORAGE_API_URL = '/api/storage';
const E14_API_URL = '/api/e14';

/**
 * Servicio para operaciones relacionadas con formularios E14
 */
class E14Service {
  /**
   * Genera una URL presignada para subir un archivo PDF
   */
  async getPresignedUploadUrl(fileName: string): Promise<PresignedUrlResponse> {
    const request: PresignedUrlRequest = {
      fileName,
      contentType: 'application/pdf',
    };
    const response = await axios.post<PresignedUrlResponse>(`${STORAGE_API_URL}/presigned-url`, request);
    return response.data;
  }

  /**
   * Genera una URL presignada para descargar un archivo
   */
  async getPresignedDownloadUrl(objectKey: string): Promise<PresignedDownloadResponse> {
    const response = await axios.get<PresignedDownloadResponse>(`${STORAGE_API_URL}/presigned-url`, {
      params: { objectKey },
    });
    return response.data;
  }

  /**
   * Sube un archivo directamente a MinIO usando la URL presignada
   */
  async uploadFile(uploadUrl: string, file: File, onProgress?: (percent: number) => void): Promise<void> {
    // Usar fetch en lugar de axios para evitar headers adicionales
    // que pueden interferir con la firma presignada de MinIO
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Upload failed: ${response.status} - ${errorText}`);
    }

    // Simular progreso al 100% ya que fetch no soporta progreso nativo
    if (onProgress) {
      onProgress(100);
    }
  }

  /**
   * Notifica al gateway que se subió un archivo E14
   */
  async notifyUpload(request: E14NotifyRequest): Promise<E14NotifyResponse> {
    const response = await axios.post<E14NotifyResponse>(`${E14_API_URL}/notify`, request);
    return response.data;
  }

  /**
   * Flujo completo de upload: obtener URL → subir archivo → notificar
   */
  async uploadE14(
    file: File,
    electionProcessId: number,
    divipolKey?: string,
    tableNumber?: string,
    onProgress?: (percent: number) => void,
  ): Promise<E14NotifyResponse> {
    // 1. Obtener presigned URL
    const presignedResponse = await this.getPresignedUploadUrl(file.name);

    if (presignedResponse.error) {
      throw new Error(presignedResponse.error);
    }

    // 2. Subir archivo a MinIO
    await this.uploadFile(presignedResponse.uploadUrl, file, onProgress);

    // 3. Notificar al gateway
    const notifyResponse = await this.notifyUpload({
      fileName: file.name,
      storageKey: presignedResponse.objectKey,
      electionProcessId,
      divipolKey,
      tableNumber,
    });

    return notifyResponse;
  }
}

export default new E14Service();
