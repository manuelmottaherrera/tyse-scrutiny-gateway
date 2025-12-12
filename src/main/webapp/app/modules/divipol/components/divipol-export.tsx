import React, { useState, useCallback } from 'react';
import { Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { toast } from 'react-toastify';
import { useAppSelector } from 'app/config/store';
import divipolService, { ExportFormat } from 'app/shared/services/divipol.service';
import { DivipolExportModal } from './divipol-export-modal';

export const DivipolExport: React.FC = () => {
  const { filters, search, departamentos } = useAppSelector(state => state.divipol);
  const [modalOpen, setModalOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [selectedFormat, setSelectedFormat] = useState<ExportFormat>('csv');

  const isSearchMode = search.active;
  const hasMultiplePages = search.pagination.totalPages > 1;

  // Determinar si hay datos para exportar
  const hasData = isSearchMode ? search.results.length > 0 : departamentos.length > 0;

  const handleExport = useCallback(
    async (format: ExportFormat, scope?: 'currentPage' | 'all') => {
      setExporting(true);
      try {
        let blob: Blob;
        let filename: string;
        const now = new Date();
        const timestamp = `${now.toISOString().slice(0, 10)}_${now.toTimeString().slice(0, 8).replace(/:/g, '-')}`;

        if (isSearchMode) {
          // Modo búsqueda
          blob = await divipolService.exportSearch(format, {
            q: search.term,
            mode: search.mode,
            page: search.pagination.page,
            size: search.pagination.size,
            exportAll: scope === 'all',
          });
          filename = `divipol-busqueda-${timestamp}.${format}`;
        } else {
          // Modo filtros
          blob = await divipolService.exportFilters(format, {
            codDepto: filters.departamento ?? undefined,
            codMpio: filters.municipio ?? undefined,
            codZona: filters.zona ?? undefined,
          });
          filename = `divipol-reporte-${timestamp}.${format}`;
        }

        // Descargar archivo
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        toast.success(<Translate contentKey="divipol.export.success">Reporte descargado exitosamente</Translate>);
        setModalOpen(false);
      } catch (error) {
        console.error('Error exporting:', error);
        toast.error(<Translate contentKey="divipol.export.error">Error al generar el reporte</Translate>);
      } finally {
        setExporting(false);
      }
    },
    [isSearchMode, search, filters],
  );

  // Exportación directa o con modal según el contexto
  const handleQuickExport = (format: ExportFormat) => {
    setSelectedFormat(format);
    if (isSearchMode && hasMultiplePages) {
      // Abrir modal para preguntar alcance
      setModalOpen(true);
    } else {
      // Exportar directamente
      handleExport(format);
    }
  };

  if (!hasData) {
    return null;
  }

  return (
    <div className="divipol-export mt-3 text-end">
      <Dropdown isOpen={dropdownOpen} toggle={() => setDropdownOpen(!dropdownOpen)}>
        <DropdownToggle color="primary" caret disabled={exporting}>
          {exporting ? (
            <>
              <span className="spinner-border spinner-border-sm me-2" />
              <Translate contentKey="divipol.export.exporting">Exportando...</Translate>
            </>
          ) : (
            <>
              <i className="bi bi-download me-2" />
              <Translate contentKey="divipol.export.download">Descargar Reporte</Translate>
            </>
          )}
        </DropdownToggle>
        <DropdownMenu end>
          <DropdownItem onClick={() => handleQuickExport('csv')}>
            <i className="bi bi-filetype-csv me-2" />
            CSV
          </DropdownItem>
          <DropdownItem onClick={() => handleQuickExport('pdf')}>
            <i className="bi bi-filetype-pdf me-2" />
            PDF
          </DropdownItem>
        </DropdownMenu>
      </Dropdown>

      <DivipolExportModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onExport={(format, scope) => handleExport(format, scope)}
        showScopeOption={isSearchMode && hasMultiplePages}
        totalPages={search.pagination.totalPages}
        currentPage={search.pagination.page}
        totalElements={search.pagination.totalElements}
        exporting={exporting}
      />
    </div>
  );
};
