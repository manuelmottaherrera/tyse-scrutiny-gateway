import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Button, Table, Badge, Input, InputGroup, InputGroupText } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getPermissions } from 'app/shared/reducers/authorization/permission.reducer';

export const PermissionList = () => {
  const dispatch = useAppDispatch();
  const permissions = useAppSelector(state => state.permission.entities);
  const loading = useAppSelector(state => state.permission.loading);
  const [filterText, setFilterText] = useState('');
  const [showInactive, setShowInactive] = useState(false);

  useEffect(() => {
    dispatch(getPermissions());
  }, []);

  const handleSyncList = () => {
    dispatch(getPermissions());
  };

  const filteredPermissions = permissions.filter(permission => {
    const matchesFilter =
      filterText === '' ||
      permission.name?.toLowerCase().includes(filterText.toLowerCase()) ||
      permission.resource?.toLowerCase().includes(filterText.toLowerCase()) ||
      permission.action?.toLowerCase().includes(filterText.toLowerCase());

    const matchesActiveFilter = showInactive || permission.isActive;

    return matchesFilter && matchesActiveFilter;
  });

  return (
    <div>
      <h2 id="permission-heading" data-cy="PermissionHeading">
        <Translate contentKey="authorization.permission.home.title">Permissions</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="authorization.permission.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/admin/permission/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="authorization.permission.home.createLabel">Create new Permission</Translate>
          </Link>
        </div>
      </h2>

      <div className="mb-3">
        <InputGroup>
          <InputGroupText>
            <FontAwesomeIcon icon="search" />
          </InputGroupText>
          <Input
            type="text"
            placeholder="Buscar por nombre, recurso o acción..."
            value={filterText}
            onChange={e => setFilterText(e.target.value)}
          />
        </InputGroup>
        <div className="form-check mt-2">
          <input
            className="form-check-input"
            type="checkbox"
            id="showInactive"
            checked={showInactive}
            onChange={e => setShowInactive(e.target.checked)}
          />
          <label className="form-check-label" htmlFor="showInactive">
            <Translate contentKey="authorization.permission.showInactive">Show inactive permissions</Translate>
          </label>
        </div>
      </div>

      <div className="table-responsive">
        {filteredPermissions && filteredPermissions.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="authorization.permission.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.permission.name">Name</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.permission.resource">Resource</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.permission.action">Action</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.permission.isActive">Active</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {filteredPermissions.map((permission, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/admin/permission/${permission.id}`} color="link" size="sm">
                      {permission.id}
                    </Button>
                  </td>
                  <td>{permission.name}</td>
                  <td>
                    <Badge color="secondary">{permission.resource}</Badge>
                  </td>
                  <td>
                    <Badge color="primary">{permission.action}</Badge>
                  </td>
                  <td>
                    {permission.isActive ? (
                      <Badge color="success">
                        <Translate contentKey="authorization.permission.active">Active</Translate>
                      </Badge>
                    ) : (
                      <Badge color="secondary">
                        <Translate contentKey="authorization.permission.inactive">Inactive</Translate>
                      </Badge>
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/admin/permission/${permission.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/admin/permission/${permission.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (location.href = `/admin/permission/${permission.id}/delete`)}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="authorization.permission.home.notFound">No Permissions found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default PermissionList;
