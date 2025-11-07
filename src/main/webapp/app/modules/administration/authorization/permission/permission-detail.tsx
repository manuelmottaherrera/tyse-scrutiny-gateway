import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col, Badge } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getPermission } from 'app/shared/reducers/authorization/permission.reducer';

export const PermissionDetail = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();

  useEffect(() => {
    if (id) {
      dispatch(getPermission(Number(id)));
    }
  }, [id]);

  const permission = useAppSelector(state => state.permission.entity);

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="permissionDetailsHeading">
          <Translate contentKey="authorization.permission.detail.title">Permission</Translate>
        </h2>
        {permission && (
          <dl className="jh-entity-details">
            <dt>
              <span id="id">
                <Translate contentKey="authorization.permission.id">ID</Translate>
              </span>
            </dt>
            <dd>{permission.id}</dd>
            <dt>
              <span id="name">
                <Translate contentKey="authorization.permission.name">Name</Translate>
              </span>
            </dt>
            <dd>{permission.name}</dd>
            <dt>
              <span id="resource">
                <Translate contentKey="authorization.permission.resource">Resource</Translate>
              </span>
            </dt>
            <dd>
              <Badge color="secondary">{permission.resource}</Badge>
            </dd>
            <dt>
              <span id="action">
                <Translate contentKey="authorization.permission.action">Action</Translate>
              </span>
            </dt>
            <dd>
              <Badge color="primary">{permission.action}</Badge>
            </dd>
            <dt>
              <span id="description">
                <Translate contentKey="authorization.permission.description">Description</Translate>
              </span>
            </dt>
            <dd>{permission.description}</dd>
            <dt>
              <span id="isActive">
                <Translate contentKey="authorization.permission.isActive">Active</Translate>
              </span>
            </dt>
            <dd>
              {permission.isActive ? (
                <Badge color="success">
                  <Translate contentKey="authorization.permission.active">Active</Translate>
                </Badge>
              ) : (
                <Badge color="secondary">
                  <Translate contentKey="authorization.permission.inactive">Inactive</Translate>
                </Badge>
              )}
            </dd>
            {permission.createdDate && (
              <>
                <dt>
                  <Translate contentKey="authorization.permission.createdDate">Created Date</Translate>
                </dt>
                <dd>
                  <TextFormat value={permission.createdDate} type="date" format={APP_DATE_FORMAT} />
                </dd>
              </>
            )}
          </dl>
        )}
        <Button tag={Link} to="/admin/permission" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/admin/permission/${permission?.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PermissionDetail;
