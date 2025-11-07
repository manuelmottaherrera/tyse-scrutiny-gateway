import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Button, Table, Badge } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getAuthorities } from 'app/shared/reducers/authorization/authority.reducer';

export const AuthorityList = () => {
  const dispatch = useAppDispatch();
  const authorities = useAppSelector(state => state.authority.entities);
  const loading = useAppSelector(state => state.authority.loading);

  useEffect(() => {
    dispatch(getAuthorities());
  }, []);

  const handleSyncList = () => {
    dispatch(getAuthorities());
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'SYSTEM':
        return 'warning';
      case 'CUSTOM':
        return 'info';
      case 'TENANT_SPECIFIC':
        return 'primary';
      default:
        return 'secondary';
    }
  };

  return (
    <div>
      <h2 id="authority-heading" data-cy="AuthorityHeading">
        <Translate contentKey="authorization.authority.home.title">Authorities</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="authorization.authority.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/admin/authority/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="authorization.authority.home.createLabel">Create new Authority</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {authorities && authorities.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="authorization.authority.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.name">Name</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.code">Code</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.category">Category</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.hierarchyLevel">Hierarchy Level</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.isActive">Active</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.authority.isSystem">System</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {authorities.map((authority, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/admin/authority/${authority.id}`} color="link" size="sm">
                      {authority.id}
                    </Button>
                  </td>
                  <td>{authority.name}</td>
                  <td>{authority.code}</td>
                  <td>
                    <Badge color={getCategoryColor(authority.category)}>{authority.category}</Badge>
                  </td>
                  <td>{authority.hierarchyLevel}</td>
                  <td>
                    {authority.isActive ? (
                      <Badge color="success">
                        <Translate contentKey="authorization.authority.active">Active</Translate>
                      </Badge>
                    ) : (
                      <Badge color="secondary">
                        <Translate contentKey="authorization.authority.inactive">Inactive</Translate>
                      </Badge>
                    )}
                  </td>
                  <td>
                    {authority.isSystem && (
                      <Badge color="warning">
                        <Translate contentKey="authorization.authority.system">System</Translate>
                      </Badge>
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/admin/authority/${authority.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/admin/authority/${authority.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                        disabled={authority.isSystem}
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (location.href = `/admin/authority/${authority.id}/delete`)}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                        disabled={authority.isSystem}
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
              <Translate contentKey="authorization.authority.home.notFound">No Authorities found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default AuthorityList;
