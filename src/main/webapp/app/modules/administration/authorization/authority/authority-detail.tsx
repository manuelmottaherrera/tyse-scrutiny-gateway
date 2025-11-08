import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col, Badge, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getAuthority, getAuthorityPermissions } from 'app/shared/reducers/authorization/authority.reducer';

export const AuthorityDetail = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();

  useEffect(() => {
    if (id) {
      dispatch(getAuthority(Number(id)));
      dispatch(getAuthorityPermissions(Number(id)));
    }
  }, [id]);

  const authority = useAppSelector(state => state.authority.entity);
  const permissions = useAppSelector(state => state.authority.permissions);
  const loading = useAppSelector(state => state.authority.loading);
  const permissionsLoading = useAppSelector(state => state.authority.permissionsLoading);

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
    <Row>
      <Col md="8">
        <h2 data-cy="authorityDetailsHeading">
          <Translate contentKey="authorization.authority.detail.title">Authority</Translate>
        </h2>
        {authority && (
          <dl className="jh-entity-details">
            <dt>
              <span id="id">
                <Translate contentKey="authorization.authority.id">ID</Translate>
              </span>
            </dt>
            <dd>{authority.id}</dd>
            <dt>
              <span id="name">
                <Translate contentKey="authorization.authority.name">Name</Translate>
              </span>
            </dt>
            <dd>{authority.name}</dd>
            <dt>
              <span id="code">
                <Translate contentKey="authorization.authority.code">Code</Translate>
              </span>
            </dt>
            <dd>{authority.code}</dd>
            <dt>
              <span id="description">
                <Translate contentKey="authorization.authority.description">Description</Translate>
              </span>
            </dt>
            <dd>{authority.description}</dd>
            <dt>
              <span id="category">
                <Translate contentKey="authorization.authority.category">Category</Translate>
              </span>
            </dt>
            <dd>
              <Badge color={getCategoryColor(authority.category)}>{authority.category}</Badge>
            </dd>
            <dt>
              <span id="hierarchyLevel">
                <Translate contentKey="authorization.authority.hierarchyLevel">Hierarchy Level</Translate>
              </span>
            </dt>
            <dd>{authority.hierarchyLevel}</dd>
            <dt>
              <span id="isActive">
                <Translate contentKey="authorization.authority.isActive">Active</Translate>
              </span>
            </dt>
            <dd>
              {authority.isActive ? (
                <Badge color="success">
                  <Translate contentKey="authorization.authority.active">Active</Translate>
                </Badge>
              ) : (
                <Badge color="secondary">
                  <Translate contentKey="authorization.authority.inactive">Inactive</Translate>
                </Badge>
              )}
            </dd>
            <dt>
              <span id="isSystem">
                <Translate contentKey="authorization.authority.isSystem">System</Translate>
              </span>
            </dt>
            <dd>
              {authority.isSystem && (
                <Badge color="warning">
                  <Translate contentKey="authorization.authority.system">System</Translate>
                </Badge>
              )}
            </dd>
            {authority.createdBy && (
              <>
                <dt>
                  <Translate contentKey="authorization.authority.createdBy">Created By</Translate>
                </dt>
                <dd>{authority.createdBy}</dd>
              </>
            )}
            {authority.createdDate && (
              <>
                <dt>
                  <Translate contentKey="authorization.authority.createdDate">Created Date</Translate>
                </dt>
                <dd>
                  <TextFormat value={authority.createdDate} type="date" format={APP_DATE_FORMAT} />
                </dd>
              </>
            )}
            {authority.lastModifiedBy && (
              <>
                <dt>
                  <Translate contentKey="authorization.authority.lastModifiedBy">Last Modified By</Translate>
                </dt>
                <dd>{authority.lastModifiedBy}</dd>
              </>
            )}
            {authority.lastModifiedDate && (
              <>
                <dt>
                  <Translate contentKey="authorization.authority.lastModifiedDate">Last Modified Date</Translate>
                </dt>
                <dd>
                  <TextFormat value={authority.lastModifiedDate} type="date" format={APP_DATE_FORMAT} />
                </dd>
              </>
            )}
          </dl>
        )}
        <h3 className="mt-4">
          <Translate contentKey="authorization.authority.permissions">Permissions</Translate>
        </h3>
        {permissionsLoading ? (
          <p className="text-muted">
            <FontAwesomeIcon icon="spinner" spin className="me-2" />
            <Translate contentKey="authorization.authority.loadingPermissions">Loading permissions...</Translate>
          </p>
        ) : permissions && permissions.length > 0 ? (
          <Table responsive size="sm">
            <thead>
              <tr>
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
              </tr>
            </thead>
            <tbody>
              {permissions.map((permission, i) => (
                <tr key={`permission-${i}`}>
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
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          <div className="alert alert-info">
            <FontAwesomeIcon icon="info-circle" className="me-2" />
            <Translate contentKey="authorization.authority.noPermissions">
              This role has no permissions assigned yet. You can assign permissions from the permissions management page.
            </Translate>
          </div>
        )}
        <Button tag={Link} to="/admin/authority" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/admin/authority/${authority?.id}/edit`} replace color="primary" disabled={authority?.isSystem}>
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AuthorityDetail;
