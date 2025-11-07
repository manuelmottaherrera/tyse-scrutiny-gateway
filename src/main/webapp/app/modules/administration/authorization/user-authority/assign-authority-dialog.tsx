import React, { useEffect, useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Label } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getAuthorities } from 'app/shared/reducers/authorization/authority.reducer';
import { assignAuthorityToUser } from 'app/shared/reducers/authorization/user-authority.reducer';

interface AssignAuthorityDialogProps {
  userId: number;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const AssignAuthorityDialog: React.FC<AssignAuthorityDialogProps> = ({ userId, isOpen, onClose, onSuccess }) => {
  const dispatch = useAppDispatch();
  const authorities = useAppSelector(state => state.authority.entities);
  const updating = useAppSelector(state => state.userAuthority.updating);
  const updateSuccess = useAppSelector(state => state.userAuthority.updateSuccess);

  useEffect(() => {
    dispatch(getAuthorities());
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      onSuccess();
    }
  }, [updateSuccess]);

  const handleSubmit = values => {
    const assignment = {
      userId,
      authorityId: Number(values.authorityId),
      expiresAt: values.expiresAt || null,
    };

    dispatch(assignAuthorityToUser(assignment));
  };

  const activeAuthorities = authorities.filter(auth => auth.isActive);

  return (
    <Modal isOpen={isOpen} toggle={onClose}>
      <ModalHeader toggle={onClose}>
        <Translate contentKey="authorization.userAuthority.assignRole">Assign Role</Translate>
      </ModalHeader>
      <ValidatedForm onSubmit={handleSubmit} defaultValues={{}}>
        <ModalBody>
          <ValidatedField
            label={translate('authorization.authority.name')}
            id="user-authority-authorityId"
            name="authorityId"
            data-cy="authorityId"
            type="select"
            validate={{
              required: { value: true, message: translate('entity.validation.required') },
            }}
          >
            <option value="" key="0">
              {translate('global.form.select')}
            </option>
            {activeAuthorities.map(authority => (
              <option value={authority.id} key={authority.id}>
                {authority.name} - {authority.description}
              </option>
            ))}
          </ValidatedField>

          <ValidatedField
            label={translate('authorization.userAuthority.expiresAt')}
            id="user-authority-expiresAt"
            name="expiresAt"
            data-cy="expiresAt"
            type="datetime-local"
          />
          <small className="form-text text-muted">
            <Translate contentKey="authorization.userAuthority.expiresAtHelp">
              Leave empty for permanent assignment. Format: YYYY-MM-DD HH:MM
            </Translate>
          </small>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={onClose}>
            <FontAwesomeIcon icon="ban" />
            &nbsp;
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>
          <Button color="primary" type="submit" disabled={updating}>
            <FontAwesomeIcon icon="save" />
            &nbsp;
            <Translate contentKey="entity.action.save">Save</Translate>
          </Button>
        </ModalFooter>
      </ValidatedForm>
    </Modal>
  );
};

export default AssignAuthorityDialog;
