import React from 'react';
import { Button } from 'reactstrap';
import { Translate } from 'react-jhipster';

export const DivipolExport: React.FC = () => {
  const handleExport = () => {
    // TODO: Implementar exportación real (PDF/Excel)
    alert('Funcionalidad de exportación pendiente de implementar');
  };

  return (
    <div className="divipol-export mt-3 text-end">
      <Button color="primary" onClick={handleExport}>
        <i className="bi bi-download me-2"></i>
        <Translate contentKey="divipol.export.download">Descargar Reporte</Translate>
      </Button>
    </div>
  );
};
