import React, { useState, useCallback } from 'react';
import { Translate, translate } from 'react-jhipster';
import { Alert, Button, Card, CardBody, CardHeader, Col, Form, FormGroup, Input, Label, Progress, Row } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCloudUploadAlt, faFilePdf, faCheck, faTimes } from '@fortawesome/free-solid-svg-icons';

import e14Service from 'app/shared/services/e14.service';

import './e14-upload.scss';

interface UploadState {
  status: 'idle' | 'uploading' | 'success' | 'error';
  progress: number;
  message: string;
  storageKey?: string;
}

const E14UploadPage = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [electionProcessId, setElectionProcessId] = useState<number>(1);
  const [divipolKey, setDivipolKey] = useState<string>('');
  const [tableNumber, setTableNumber] = useState<string>('');
  const [uploadState, setUploadState] = useState<UploadState>({
    status: 'idle',
    progress: 0,
    message: '',
  });
  const [dragActive, setDragActive] = useState(false);

  const handleDrag = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.type === 'application/pdf') {
        setSelectedFile(file);
        setUploadState({ status: 'idle', progress: 0, message: '' });
      } else {
        setUploadState({
          status: 'error',
          progress: 0,
          message: translate('e14.upload.errorNotPdf'),
        });
      }
    }
  }, []);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.type === 'application/pdf') {
        setSelectedFile(file);
        setUploadState({ status: 'idle', progress: 0, message: '' });
      } else {
        setUploadState({
          status: 'error',
          progress: 0,
          message: translate('e14.upload.errorNotPdf'),
        });
      }
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploadState({ status: 'uploading', progress: 0, message: '' });

    try {
      const response = await e14Service.uploadE14(
        selectedFile,
        electionProcessId,
        divipolKey || undefined,
        tableNumber || undefined,
        progress => {
          setUploadState(prev => ({ ...prev, progress }));
        },
      );

      setUploadState({
        status: 'success',
        progress: 100,
        message: translate('e14.upload.success'),
        storageKey: response.storageKey,
      });
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : translate('e14.upload.errorGeneric');
      setUploadState({
        status: 'error',
        progress: 0,
        message: errorMessage,
      });
    }
  };

  const handleReset = () => {
    setSelectedFile(null);
    setUploadState({ status: 'idle', progress: 0, message: '' });
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="e14-upload-page">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <Card>
            <CardHeader>
              <h2 className="mb-0">
                <FontAwesomeIcon icon={faCloudUploadAlt} className="me-2" />
                <Translate contentKey="e14.upload.title">Subir Formulario E14</Translate>
              </h2>
            </CardHeader>
            <CardBody>
              <p className="text-muted">
                <Translate contentKey="e14.upload.description">Sube el formulario E14 escaneado en formato PDF para procesarlo.</Translate>
              </p>

              {/* Zona de drop */}
              <div
                className={`drop-zone ${dragActive ? 'drag-active' : ''} ${selectedFile ? 'has-file' : ''}`}
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
              >
                {/* Input oculto pero clickeable - fuera del contenedor con pointer-events: none */}
                {!selectedFile && (
                  <Input type="file" accept="application/pdf" onChange={handleFileSelect} className="file-input" id="e14-file-input" />
                )}
                {selectedFile ? (
                  <div className="file-preview">
                    <FontAwesomeIcon icon={faFilePdf} size="3x" className="text-danger mb-2" />
                    <p className="file-name">{selectedFile.name}</p>
                    <p className="file-size text-muted">{formatFileSize(selectedFile.size)}</p>
                    <Button color="link" size="sm" onClick={handleReset} disabled={uploadState.status === 'uploading'}>
                      <Translate contentKey="e14.upload.changeFile">Cambiar archivo</Translate>
                    </Button>
                  </div>
                ) : (
                  <div className="drop-zone-content">
                    <FontAwesomeIcon icon={faCloudUploadAlt} size="3x" className="mb-3 text-muted" />
                    <p>
                      <Translate contentKey="e14.upload.dropHere">Arrastra el archivo PDF aquí</Translate>
                    </p>
                    <p className="text-muted">
                      <Translate contentKey="e14.upload.orClick">o haz clic para seleccionar</Translate>
                    </p>
                  </div>
                )}
              </div>

              {/* Campos opcionales */}
              <Form className="mt-4">
                <Row>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="divipolKey">
                        <Translate contentKey="e14.upload.divipolKey">Código DIVIPOL</Translate>
                      </Label>
                      <Input
                        type="text"
                        id="divipolKey"
                        value={divipolKey}
                        onChange={e => setDivipolKey(e.target.value)}
                        placeholder="76-001-01-01-001"
                        disabled={uploadState.status === 'uploading'}
                      />
                    </FormGroup>
                  </Col>
                  <Col md={6}>
                    <FormGroup>
                      <Label for="tableNumber">
                        <Translate contentKey="e14.upload.tableNumber">Número de Mesa</Translate>
                      </Label>
                      <Input
                        type="text"
                        id="tableNumber"
                        value={tableNumber}
                        onChange={e => setTableNumber(e.target.value)}
                        placeholder="001"
                        disabled={uploadState.status === 'uploading'}
                      />
                    </FormGroup>
                  </Col>
                </Row>
                <FormGroup>
                  <Label for="electionProcess">
                    <Translate contentKey="e14.upload.electionProcess">Proceso Electoral</Translate>
                  </Label>
                  <Input
                    type="select"
                    id="electionProcess"
                    value={electionProcessId}
                    onChange={e => setElectionProcessId(Number(e.target.value))}
                    disabled={uploadState.status === 'uploading'}
                  >
                    <option value={1}>Elecciones Municipales 2024</option>
                  </Input>
                </FormGroup>
              </Form>

              {/* Barra de progreso */}
              {uploadState.status === 'uploading' && (
                <div className="mt-4">
                  <Progress value={uploadState.progress} animated striped color="primary">
                    {uploadState.progress}%
                  </Progress>
                  <p className="text-center mt-2 text-muted">
                    <Translate contentKey="e14.upload.uploading">Subiendo archivo...</Translate>
                  </p>
                </div>
              )}

              {/* Mensajes de estado */}
              {uploadState.status === 'success' && (
                <Alert color="success" className="mt-4">
                  <FontAwesomeIcon icon={faCheck} className="me-2" />
                  {uploadState.message}
                  {uploadState.storageKey && (
                    <small className="d-block mt-2 text-muted">
                      <Translate contentKey="e14.upload.storageKey">Clave de almacenamiento</Translate>: {uploadState.storageKey}
                    </small>
                  )}
                </Alert>
              )}

              {uploadState.status === 'error' && (
                <Alert color="danger" className="mt-4">
                  <FontAwesomeIcon icon={faTimes} className="me-2" />
                  {uploadState.message}
                </Alert>
              )}

              {/* Botón de subir */}
              <div className="mt-4 d-grid">
                <Button color="primary" size="lg" onClick={handleUpload} disabled={!selectedFile || uploadState.status === 'uploading'}>
                  {uploadState.status === 'uploading' ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      <Translate contentKey="e14.upload.uploading">Subiendo...</Translate>
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faCloudUploadAlt} className="me-2" />
                      <Translate contentKey="e14.upload.uploadButton">Subir Formulario E14</Translate>
                    </>
                  )}
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default E14UploadPage;
