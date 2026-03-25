import React from 'react';
import { Row, Col, Card, CardBody } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { APIProvider, Map, AdvancedMarker } from '@vis.gl/react-google-maps';
import { useTheme } from 'app/shared/context/theme-contex/theme-context';
import type { PuestoDetalle } from 'app/shared/services/divipol.service';
import './puesto-info-tab.scss';

const GOOGLE_MAPS_API_KEY = process.env.REACT_APP_GOOGLE_MAPS_API_KEY || '';

interface PuestoInfoTabProps {
  puesto: PuestoDetalle;
}

const hasValidCoordinates = (puesto: PuestoDetalle): boolean =>
  puesto.latitud != null && puesto.longitud != null && puesto.latitud !== 0 && puesto.longitud !== 0;

export const PuestoInfoTab: React.FC<PuestoInfoTabProps> = ({ puesto }) => {
  const { currentResolvedTheme } = useTheme();

  const generateCodigoDivipol = () => {
    const depto = String(puesto.coddepto).padStart(2, '0');
    const mpio = String(puesto.codmipio).padStart(3, '0');
    const zona = String(puesto.codzona).padStart(2, '0');
    const puestoCode = String(puesto.codpuesto).padStart(2, '0');
    return `${depto}${mpio}${zona}${puestoCode}`;
  };

  return (
    <div className="puesto-info-tab">
      {/* Información de ubicación */}
      <Card className="mb-3">
        <CardBody>
          <h6 className="text-muted mb-3">
            <i className="bi bi-geo me-2"></i>
            <Translate contentKey="divipol.puestoDetail.ubicacion">Ubicación</Translate>
          </h6>
          <Row>
            <Col md={6}>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.codigoDivipol">Código Divipol</Translate>
                </span>
                <span className="value font-monospace">{generateCodigoDivipol()}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.departamento">Departamento</Translate>
                </span>
                <span className="value">{puesto.nomdepto}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.municipio">Municipio</Translate>
                </span>
                <span className="value">{puesto.nommipio}</span>
              </div>
            </Col>
            <Col md={6}>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.zona">Zona</Translate>
                </span>
                <span className="value">{puesto.codzona}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.puesto">Puesto</Translate>
                </span>
                <span className="value">{puesto.nompuesto}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.direccion">Dirección</Translate>
                </span>
                <span className="value">{puesto.direccion || '-'}</span>
              </div>
            </Col>
          </Row>

          {/* Coordenadas si están disponibles y son válidas (no cero) */}
          {hasValidCoordinates(puesto) && (
            <div className="mt-3 pt-3 border-top">
              <div className="info-item">
                <span className="label">
                  <i className="bi bi-pin-map me-1"></i>
                  <Translate contentKey="divipol.puestoDetail.fields.coordenadas">Coordenadas</Translate>
                </span>
                <span className="value font-monospace">
                  {puesto.latitud.toFixed(6)}, {puesto.longitud.toFixed(6)}
                </span>
              </div>
            </div>
          )}
        </CardBody>
      </Card>

      {/* Mapa de Google Maps */}
      {hasValidCoordinates(puesto) && GOOGLE_MAPS_API_KEY && (
        <Card className="mb-3">
          <CardBody>
            <h6 className="text-muted mb-3">
              <i className="bi bi-map me-2"></i>
              <Translate contentKey="divipol.puestoDetail.mapa.titulo">Ubicación en el Mapa</Translate>
            </h6>
            <div className="map-container">
              <APIProvider apiKey={GOOGLE_MAPS_API_KEY}>
                <Map
                  defaultCenter={{ lat: puesto.latitud, lng: puesto.longitud }}
                  defaultZoom={16}
                  gestureHandling="cooperative"
                  disableDefaultUI={false}
                  mapId="DEMO_MAP_ID"
                  colorScheme={currentResolvedTheme === 'dark' ? 'DARK' : 'LIGHT'}
                >
                  <AdvancedMarker position={{ lat: puesto.latitud, lng: puesto.longitud }} title={puesto.nompuesto} />
                </Map>
              </APIProvider>
            </div>
            <div className="map-footer">
              <a
                href={`https://www.google.com/maps?q=${puesto.latitud},${puesto.longitud}`}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-sm btn-outline-primary"
              >
                <i className="bi bi-box-arrow-up-right me-1"></i>
                <Translate contentKey="divipol.puestoDetail.mapa.abrirGoogleMaps">Abrir en Google Maps</Translate>
              </a>
            </div>
          </CardBody>
        </Card>
      )}

      {/* Datos electorales */}
      <Card className="mb-3">
        <CardBody>
          <h6 className="text-muted mb-3">
            <i className="bi bi-clipboard-data me-2"></i>
            <Translate contentKey="divipol.puestoDetail.datosElectorales">Datos Electorales</Translate>
          </h6>
          <Row>
            <Col sm={6} md={3}>
              <div className="stat-item">
                <span className="stat-value">{puesto.nummesas}</span>
                <span className="stat-label">
                  <Translate contentKey="divipol.puestoDetail.fields.mesas">Mesas</Translate>
                </span>
              </div>
            </Col>
            <Col sm={6} md={3}>
              <div className="stat-item">
                <span className="stat-value">{puesto.pottotal?.toLocaleString('es-CO')}</span>
                <span className="stat-label">
                  <Translate contentKey="divipol.puestoDetail.fields.potencialTotal">Potencial Total</Translate>
                </span>
              </div>
            </Col>
            <Col sm={6} md={3}>
              <div className="stat-item">
                <span className="stat-value">{puesto.potfemenino?.toLocaleString('es-CO')}</span>
                <span className="stat-label">
                  <Translate contentKey="divipol.puestoDetail.fields.potencialFemenino">Pot. Femenino</Translate>
                </span>
              </div>
            </Col>
            <Col sm={6} md={3}>
              <div className="stat-item">
                <span className="stat-value">{puesto.potmasculino?.toLocaleString('es-CO')}</span>
                <span className="stat-label">
                  <Translate contentKey="divipol.puestoDetail.fields.potencialMasculino">Pot. Masculino</Translate>
                </span>
              </div>
            </Col>
          </Row>
        </CardBody>
      </Card>

      {/* Información adicional */}
      <Card>
        <CardBody>
          <h6 className="text-muted mb-3">
            <i className="bi bi-info-square me-2"></i>
            <Translate contentKey="divipol.puestoDetail.infoAdicional">Información Adicional</Translate>
          </h6>
          <Row>
            <Col md={6}>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.jal">Código JAL</Translate>
                </span>
                <span className="value">{puesto.jal || '-'}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.nomjal">Nombre JAL</Translate>
                </span>
                <span className="value">{puesto.nomjal || '-'}</span>
              </div>
            </Col>
            <Col md={6}>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.indicador">Indicador</Translate>
                </span>
                <span className="value">{puesto.indicador ?? '-'}</span>
              </div>
              <div className="info-item">
                <span className="label">
                  <Translate contentKey="divipol.puestoDetail.fields.expandida">Expandida</Translate>
                </span>
                <span className="value">{puesto.expandida ?? '-'}</span>
              </div>
            </Col>
          </Row>
        </CardBody>
      </Card>
    </div>
  );
};

export default PuestoInfoTab;
