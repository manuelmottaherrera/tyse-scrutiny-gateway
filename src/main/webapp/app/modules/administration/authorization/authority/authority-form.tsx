import React, { useEffect, useMemo } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row, Alert } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { createAuthority, getAuthority, updateAuthority, reset, clearError } from 'app/shared/reducers/authorization/authority.reducer';
import { AuthorityCategory } from 'app/shared/model/enumerations/authority-category.model';

export const AuthorityForm = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  useEffect(() => {
    if (!isNew) {
      dispatch(getAuthority(Number(id)));
    }
  }, [id]);

  useEffect(() => {
    // Clear error message when component unmounts
    return () => {
      dispatch(reset());
    };
  }, []);

  const authority = useAppSelector(state => state.authority.entity);
  const loading = useAppSelector(state => state.authority.loading);
  const updating = useAppSelector(state => state.authority.updating);
  const updateSuccess = useAppSelector(state => state.authority.updateSuccess);
  const errorMessage = useAppSelector(state => state.authority.errorMessage);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const handleClose = () => {
    navigate('/admin/authority');
  };

  const saveEntity = values => {
    const entity = {
      ...authority,
      ...values,
    };

    if (isNew) {
      dispatch(createAuthority(entity));
    } else {
      dispatch(updateAuthority(entity));
    }
  };

  const defaultValues = useMemo(
    () =>
      isNew
        ? {
            isActive: true,
            isSystem: false,
            hierarchyLevel: 1,
            category: AuthorityCategory.CUSTOM,
          }
        : {
            ...authority,
          },
    [isNew, authority],
  );

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="tysescrutinygatewayApp.authority.home.createOrEditLabel" data-cy="AuthorityCreateUpdateHeading">
            <Translate contentKey="authorization.authority.home.createOrEditLabel">Create or edit an Authority</Translate>
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
                <Alert color="danger" className="d-flex justify-content-between align-items-center">
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
                  id="authority-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('authorization.authority.name')}
                id="authority-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                }}
                disabled={authority?.isSystem}
              />
              <ValidatedField
                label={translate('authorization.authority.code')}
                id="authority-code"
                name="code"
                data-cy="code"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
                  pattern: {
                    value: /^ROLE_[A-Z_]+$/,
                    message: translate('authorization.authority.validation.codePattern'),
                  },
                }}
                disabled={authority?.isSystem}
              />
              <small className="form-text text-muted">
                <Translate contentKey="authorization.authority.codeHelp">
                  El código debe comenzar con ROLE_ (ejemplo: ROLE_MANAGER)
                </Translate>
              </small>
              <ValidatedField
                label={translate('authorization.authority.description')}
                id="authority-description"
                name="description"
                data-cy="description"
                type="textarea"
                validate={{
                  maxLength: { value: 500, message: translate('entity.validation.maxlength', { max: 500 }) },
                }}
              />
              <ValidatedField
                label={translate('authorization.authority.category')}
                id="authority-category"
                name="category"
                data-cy="category"
                type="select"
                disabled={authority?.isSystem}
              >
                <option value={AuthorityCategory.SYSTEM}>{translate('authorization.authorityCategory.SYSTEM')}</option>
                <option value={AuthorityCategory.CUSTOM}>{translate('authorization.authorityCategory.CUSTOM')}</option>
                <option value={AuthorityCategory.TENANT_SPECIFIC}>{translate('authorization.authorityCategory.TENANT_SPECIFIC')}</option>
              </ValidatedField>
              <ValidatedField
                label={translate('authorization.authority.hierarchyLevel')}
                id="authority-hierarchyLevel"
                name="hierarchyLevel"
                data-cy="hierarchyLevel"
                type="number"
                validate={{
                  min: { value: 1, message: translate('entity.validation.min', { min: 1 }) },
                  max: { value: 1000, message: translate('entity.validation.max', { max: 1000 }) },
                }}
              />
              <ValidatedField
                label={translate('authorization.authority.isActive')}
                id="authority-isActive"
                name="isActive"
                data-cy="isActive"
                check
                type="checkbox"
              />
              {authority?.isSystem && (
                <ValidatedField
                  label={translate('authorization.authority.isSystem')}
                  id="authority-isSystem"
                  name="isSystem"
                  data-cy="isSystem"
                  check
                  type="checkbox"
                  disabled
                />
              )}
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/admin/authority" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button
                color="primary"
                id="save-entity"
                data-cy="entityCreateSaveButton"
                type="submit"
                disabled={updating || authority?.isSystem}
              >
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

export default AuthorityForm;
