import React, { useEffect, useMemo } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row, Alert } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { createPermission, getPermission, updatePermission, reset, clearError } from 'app/shared/reducers/authorization/permission.reducer';

export const PermissionForm = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  useEffect(() => {
    if (!isNew) {
      dispatch(getPermission(Number(id)));
    }
  }, [id]);

  useEffect(() => {
    // Clear error message when component unmounts
    return () => {
      dispatch(reset());
    };
  }, []);

  const permission = useAppSelector(state => state.permission.entity);
  const loading = useAppSelector(state => state.permission.loading);
  const updating = useAppSelector(state => state.permission.updating);
  const updateSuccess = useAppSelector(state => state.permission.updateSuccess);
  const errorMessage = useAppSelector(state => state.permission.errorMessage);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const handleClose = () => {
    navigate('/admin/permission');
  };

  const saveEntity = values => {
    // Construir el nombre automáticamente como resource.action
    const entity = {
      ...permission,
      ...values,
      name: `${values.resource}.${values.action}`,
    };

    if (isNew) {
      dispatch(createPermission(entity));
    } else {
      dispatch(updatePermission(entity));
    }
  };

  const defaultValues = useMemo(
    () =>
      isNew
        ? {
            isActive: true,
          }
        : {
            ...permission,
          },
    [isNew, permission],
  );

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tysescrutinygatewayApp.permission.home.createOrEditLabel" data-cy="PermissionCreateUpdateHeading">
            <Translate contentKey="authorization.permission.home.createOrEditLabel">Create or edit a Permission</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues} onSubmit={saveEntity}>
              {errorMessage && (
                <Alert color="danger" className="d-flex justify-content-between align-items-center" fade={false}>
                  <div>
                    <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
                    {errorMessage.startsWith('error.') ? <Translate contentKey={errorMessage} /> : errorMessage}
                  </div>
                  <Button close onClick={() => dispatch(clearError())} />
                </Alert>
              )}
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="permission-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('authorization.permission.resource')}
                id="permission-resource"
                name="resource"
                data-cy="resource"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                  pattern: {
                    value: /^[a-z][a-z0-9_]*$/,
                    message: translate('authorization.permission.validation.resourcePattern'),
                  },
                }}
              />
              <small className="form-text text-muted">
                <Translate contentKey="authorization.permission.resourceHelp">
                  Resource name in lowercase (e.g., user, report, authority)
                </Translate>
              </small>
              <ValidatedField
                label={translate('authorization.permission.action')}
                id="permission-action"
                name="action"
                data-cy="action"
                type="select"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              >
                <option value="" key="0">
                  {translate('authorization.permission.selectAction')}
                </option>
                <option value="create" key="create">
                  create
                </option>
                <option value="read" key="read">
                  read
                </option>
                <option value="update" key="update">
                  update
                </option>
                <option value="delete" key="delete">
                  delete
                </option>
                <option value="execute" key="execute">
                  execute
                </option>
                <option value="manage" key="manage">
                  manage
                </option>
                <option value="list" key="list">
                  list
                </option>
              </ValidatedField>
              <small className="form-text text-muted">
                <Translate contentKey="authorization.permission.actionHelp">Select the action type for this permission</Translate>
              </small>
              <div className="alert alert-info mt-2 mb-3">
                <FontAwesomeIcon icon="info-circle" />{' '}
                <Translate contentKey="authorization.permission.nameAutoGenerated">
                  The permission name will be automatically generated as: resource.action
                </Translate>
              </div>
              <ValidatedField
                label={translate('authorization.permission.description')}
                id="permission-description"
                name="description"
                data-cy="description"
                type="textarea"
                validate={{
                  maxLength: { value: 500, message: translate('entity.validation.maxlength', { max: 500 }) },
                }}
              />
              <ValidatedField
                label={translate('authorization.permission.isActive')}
                id="permission-isActive"
                name="isActive"
                data-cy="isActive"
                check
                type="checkbox"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/admin/permission" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PermissionForm;
