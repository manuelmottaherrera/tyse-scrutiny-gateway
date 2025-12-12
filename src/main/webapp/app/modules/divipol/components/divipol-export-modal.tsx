import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, FormGroup, Label, Input } from 'reactstrap';
import { Translate } from 'react-jhipster';
import type { ExportFormat } from 'app/shared/services/divipol.service';

interface DivipolExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onExport: (format: ExportFormat, scope?: 'currentPage' | 'all') => void;
  showScopeOption: boolean;
  totalPages: number;
  currentPage: number;
  totalElements: number;
  exporting: boolean;
}

export const DivipolExportModal: React.FC<DivipolExportModalProps> = ({
  isOpen,
  onClose,
  onExport,
  showScopeOption,
  totalPages,
  currentPage,
  totalElements,
  exporting,
}) => {
  const [format, setFormat] = useState<ExportFormat>('csv');
  const [scope, setScope] = useState<'currentPage' | 'all'>('currentPage');

  const handleExport = () => {
    onExport(format, showScopeOption ? scope : undefined);
  };

  const handleClose = () => {
    if (!exporting) {
      onClose();
    }
  };

  return (
    <Modal isOpen={isOpen} toggle={handleClose} centered>
      <ModalHeader toggle={handleClose}>
        <Translate contentKey="divipol.export.modalTitle">Exportar Reporte</Translate>
      </ModalHeader>
      <ModalBody>
        <FormGroup>
          <Label className="fw-bold">
            <Translate contentKey="divipol.export.format">Formato</Translate>
          </Label>
          <div className="d-flex gap-4 mt-2">
            <FormGroup check>
              <Label check className="d-flex align-items-center gap-2">
                <Input
                  type="radio"
                  name="format"
                  value="csv"
                  checked={format === 'csv'}
                  onChange={() => setFormat('csv')}
                  disabled={exporting}
                />
                <i className="bi bi-filetype-csv fs-5"></i>
                CSV
              </Label>
            </FormGroup>
            <FormGroup check>
              <Label check className="d-flex align-items-center gap-2">
                <Input
                  type="radio"
                  name="format"
                  value="pdf"
                  checked={format === 'pdf'}
                  onChange={() => setFormat('pdf')}
                  disabled={exporting}
                />
                <i className="bi bi-filetype-pdf fs-5"></i>
                PDF
              </Label>
            </FormGroup>
          </div>
        </FormGroup>

        {showScopeOption && (
          <FormGroup className="mt-4">
            <Label className="fw-bold">
              <Translate contentKey="divipol.export.scope">Alcance de la exportacion</Translate>
            </Label>
            <div className="mt-2">
              <FormGroup check className="mb-2">
                <Label check>
                  <Input
                    type="radio"
                    name="scope"
                    value="currentPage"
                    checked={scope === 'currentPage'}
                    onChange={() => setScope('currentPage')}
                    disabled={exporting}
                  />
                  <Translate contentKey="divipol.export.currentPage" interpolate={{ page: String(currentPage + 1) }}>
                    {`Pagina actual (${currentPage + 1})`}
                  </Translate>
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    name="scope"
                    value="all"
                    checked={scope === 'all'}
                    onChange={() => setScope('all')}
                    disabled={exporting}
                  />
                  <Translate contentKey="divipol.export.allResults" interpolate={{ total: totalElements.toLocaleString() }}>
                    {`Todos los resultados (${totalElements.toLocaleString()} registros)`}
                  </Translate>
                  {totalElements > 10000 && (
                    <small className="text-muted d-block ms-4">
                      <Translate contentKey="divipol.export.maxRecords">Maximo 10,000 registros</Translate>
                    </small>
                  )}
                </Label>
              </FormGroup>
            </div>
          </FormGroup>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={exporting}>
          <Translate contentKey="entity.action.cancel">Cancelar</Translate>
        </Button>
        <Button color="primary" onClick={handleExport} disabled={exporting}>
          {exporting ? (
            <>
              <span className="spinner-border spinner-border-sm me-2" />
              <Translate contentKey="divipol.export.exporting">Exportando...</Translate>
            </>
          ) : (
            <>
              <i className="bi bi-download me-2" />
              <Translate contentKey="divipol.export.download">Descargar</Translate>
            </>
          )}
        </Button>
      </ModalFooter>
    </Modal>
  );
};
