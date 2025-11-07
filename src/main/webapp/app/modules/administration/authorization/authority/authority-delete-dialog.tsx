import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getAuthority, deleteAuthority } from 'app/shared/reducers/authorization/authority.reducer';

export const AuthorityDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  useEffect(() => {
    if (id) {
      dispatch(getAuthority(Number(id)));
    }
  }, [id]);

  const authority = useAppSelector(state => state.authority.entity);
  const updateSuccess = useAppSelector(state => state.authority.updateSuccess);

  const handleClose = () => {
    navigate('/admin/authority');
  };

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    if (id) {
      dispatch(deleteAuthority(Number(id)));
    }
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="authorityDeleteDialogHeading">
        <Translate contentKey="entity.delete.title">Confirm delete operation</Translate>
      </ModalHeader>
      <ModalBody id="tysescrutinygatewayApp.authority.delete.question">
        <Translate contentKey="authorization.authority.delete.question" interpolate={{ id: authority?.name }}>
          Are you sure you want to delete this Authority?
        </Translate>
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp;
          <Translate contentKey="entity.action.cancel">Cancel</Translate>
        </Button>
        <Button id="jhi-confirm-delete-authority" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp;
          <Translate contentKey="entity.action.delete">Delete</Translate>
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default AuthorityDeleteDialog;
