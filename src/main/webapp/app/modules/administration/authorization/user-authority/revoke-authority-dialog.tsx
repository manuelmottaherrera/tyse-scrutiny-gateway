import React, { useEffect } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { revokeAuthorityFromUser } from 'app/shared/reducers/authorization/user-authority.reducer';

interface RevokeAuthorityDialogProps {
  userAuthorityId: number;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const RevokeAuthorityDialog: React.FC<RevokeAuthorityDialogProps> = ({ userAuthorityId, isOpen, onClose, onSuccess }) => {
  const dispatch = useAppDispatch();
  const updating = useAppSelector(state => state.userAuthority.updating);
  const updateSuccess = useAppSelector(state => state.userAuthority.updateSuccess);

  useEffect(() => {
    if (updateSuccess) {
      onSuccess();
    }
  }, [updateSuccess]);

  const handleSubmit = values => {
    dispatch(revokeAuthorityFromUser({ id: userAuthorityId, reason: values.revokedReason }));
  };

  return (
    <Modal isOpen={isOpen} toggle={onClose}>
      <ModalHeader toggle={onClose}>
        <Translate contentKey="authorization.userAuthority.revokeRole">Revoke Role</Translate>
      </ModalHeader>
      <ValidatedForm onSubmit={handleSubmit} defaultValues={{}}>
        <ModalBody>
          <div className="alert alert-warning">
            <FontAwesomeIcon icon="exclamation-triangle" />{' '}
            <Translate contentKey="authorization.userAuthority.revokeConfirmation">
              Are you sure you want to revoke this role assignment? This action cannot be undone.
            </Translate>
          </div>

          <ValidatedField
            label={translate('authorization.userAuthority.revokedReason')}
            id="user-authority-revokedReason"
            name="revokedReason"
            data-cy="revokedReason"
            type="textarea"
            rows={3}
            validate={{
              required: { value: true, message: translate('entity.validation.required') },
              maxLength: { value: 500, message: translate('entity.validation.maxlength', { max: 500 }) },
            }}
          />
          <small className="form-text text-muted">
            <Translate contentKey="authorization.userAuthority.revokedReasonHelp">
              Please provide a reason for revoking this role (e.g., &quot;User changed department&quot;, &quot;Security policy
              violation&quot;)
            </Translate>
          </small>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={onClose}>
            <FontAwesomeIcon icon="ban" />
            &nbsp;
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>
          <Button color="danger" type="submit" disabled={updating}>
            <FontAwesomeIcon icon="trash" />
            &nbsp;
            <Translate contentKey="authorization.userAuthority.revokeRole">Revoke Role</Translate>
          </Button>
        </ModalFooter>
      </ValidatedForm>
    </Modal>
  );
};

export default RevokeAuthorityDialog;
