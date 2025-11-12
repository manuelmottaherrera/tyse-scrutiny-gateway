import React, { useEffect, useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Table, Badge, Input, FormGroup, Label } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getPermissions } from 'app/shared/reducers/authorization/permission.reducer';
import {
  assignPermissionToAuthority,
  revokePermissionFromAuthority,
  getAuthorityPermissions,
} from 'app/shared/reducers/authorization/authority.reducer';

interface ManagePermissionsDialogProps {
  authorityId: number;
  authorityName: string;
  isOpen: boolean;
  onClose: () => void;
}

export const ManagePermissionsDialog: React.FC<ManagePermissionsDialogProps> = ({ authorityId, authorityName, isOpen, onClose }) => {
  const dispatch = useAppDispatch();
  const allPermissions = useAppSelector(state => state.permission.entities);
  const assignedPermissions = useAppSelector(state => state.authority.permissions);
  const loading = useAppSelector(state => state.permission.loading);
  const updating = useAppSelector(state => state.authority.updating);

  const [searchTerm, setSearchTerm] = useState('');
  const [resourceFilter, setResourceFilter] = useState('');

  useEffect(() => {
    if (isOpen) {
      dispatch(getPermissions());
      dispatch(getAuthorityPermissions(authorityId));
    }
  }, [isOpen, authorityId]);

  const handleTogglePermission = (permissionId: number, isAssigned: boolean) => {
    if (isAssigned) {
      dispatch(revokePermissionFromAuthority({ authorityId, permissionId })).then(() => {
        dispatch(getAuthorityPermissions(authorityId));
      });
    } else {
      dispatch(assignPermissionToAuthority({ authorityId, permissionId })).then(() => {
        dispatch(getAuthorityPermissions(authorityId));
      });
    }
  };

  const isPermissionAssigned = (permissionId: number) => {
    return assignedPermissions.some(p => p.id === permissionId);
  };

  // Get unique resources for filter
  const resourcesSet = new Set<string>();
  allPermissions.forEach(p => {
    if (p.resource) {
      resourcesSet.add(p.resource);
    }
  });
  const uniqueResources = Array.from(resourcesSet).sort();

  // Filter permissions
  const filteredPermissions = allPermissions.filter(permission => {
    const matchesSearch =
      !searchTerm ||
      permission.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      permission.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesResource = !resourceFilter || permission.resource === resourceFilter;
    return matchesSearch && matchesResource && permission.isActive;
  });

  // Group by resource
  const groupedPermissions = filteredPermissions.reduce(
    (acc, permission) => {
      const resource = permission.resource || 'other';
      if (!acc[resource]) {
        acc[resource] = [];
      }
      acc[resource].push(permission);
      return acc;
    },
    {} as Record<string, typeof allPermissions>,
  );

  return (
    <Modal isOpen={isOpen} toggle={onClose} size="lg">
      <ModalHeader toggle={onClose}>
        <Translate contentKey="authorization.authority.managePermissions.title">Manage Permissions</Translate>
        {' - '}
        <strong>{authorityName}</strong>
      </ModalHeader>
      <ModalBody>
        <div className="mb-3">
          <FormGroup>
            <Label for="search">
              <Translate contentKey="authorization.authority.managePermissions.search">Search</Translate>
            </Label>
            <Input
              type="text"
              id="search"
              placeholder={translate('authorization.authority.managePermissions.searchPlaceholder')}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </FormGroup>

          <FormGroup>
            <Label for="resourceFilter">
              <Translate contentKey="authorization.authority.managePermissions.filterByResource">Filter by Resource</Translate>
            </Label>
            <Input type="select" id="resourceFilter" value={resourceFilter} onChange={e => setResourceFilter(e.target.value)}>
              <option value="">{translate('authorization.authority.managePermissions.allResources')}</option>
              {uniqueResources.map(resource => (
                <option key={resource} value={resource}>
                  {resource}
                </option>
              ))}
            </Input>
          </FormGroup>
        </div>

        {loading ? (
          <p className="text-center">
            <FontAwesomeIcon icon="spinner" spin className="me-2" />
            <Translate contentKey="authorization.authority.managePermissions.loading">Loading permissions...</Translate>
          </p>
        ) : (
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {Object.keys(groupedPermissions).length > 0 ? (
              Object.keys(groupedPermissions)
                .sort()
                .map(resource => (
                  <div key={resource} className="mb-3">
                    <h5 className="text-primary">
                      <Badge color="secondary" className="me-2">
                        {resource}
                      </Badge>
                    </h5>
                    <Table size="sm" bordered hover>
                      <thead>
                        <tr>
                          <th style={{ width: '50px' }}>
                            <Translate contentKey="authorization.authority.managePermissions.assigned">Assigned</Translate>
                          </th>
                          <th>
                            <Translate contentKey="authorization.permission.name">Name</Translate>
                          </th>
                          <th>
                            <Translate contentKey="authorization.permission.action">Action</Translate>
                          </th>
                          <th>
                            <Translate contentKey="authorization.permission.description">Description</Translate>
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {groupedPermissions[resource].map(permission => {
                          const isAssigned = isPermissionAssigned(permission.id);
                          return (
                            <tr key={permission.id} className={isAssigned ? 'table-active' : ''}>
                              <td className="text-center">
                                <Input
                                  type="checkbox"
                                  checked={isAssigned}
                                  onChange={() => handleTogglePermission(permission.id, isAssigned)}
                                  disabled={updating}
                                />
                              </td>
                              <td>
                                <code>{permission.name}</code>
                              </td>
                              <td>
                                <Badge color="primary">{permission.action}</Badge>
                              </td>
                              <td className="text-muted small">{permission.description}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </Table>
                  </div>
                ))
            ) : (
              <div className="alert alert-info">
                <FontAwesomeIcon icon="info-circle" className="me-2" />
                <Translate contentKey="authorization.authority.managePermissions.noResults">No permissions found</Translate>
              </div>
            )}
          </div>
        )}

        <div className="mt-3 text-muted small">
          <FontAwesomeIcon icon="info-circle" className="me-2" />
          <Translate contentKey="authorization.authority.managePermissions.info">
            Click on the checkboxes to assign or revoke permissions from this role
          </Translate>
        </div>
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={onClose}>
          <FontAwesomeIcon icon="times" />
          &nbsp;
          <Translate contentKey="entity.action.close">Close</Translate>
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default ManagePermissionsDialog;
