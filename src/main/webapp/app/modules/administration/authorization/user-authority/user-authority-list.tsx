import React, { useEffect, useState } from 'react';
import { Button, Table, Badge } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getUserAuthoritiesByUser } from 'app/shared/reducers/authorization/user-authority.reducer';
import AssignAuthorityDialog from './assign-authority-dialog';
import RevokeAuthorityDialog from './revoke-authority-dialog';

interface UserAuthorityListProps {
  userId: number;
}

export const UserAuthorityList: React.FC<UserAuthorityListProps> = ({ userId }) => {
  const dispatch = useAppDispatch();
  const userAuthorities = useAppSelector(state => state.userAuthority.entities);
  const loading = useAppSelector(state => state.userAuthority.loading);
  const [showAssignDialog, setShowAssignDialog] = useState(false);
  const [revokeDialogId, setRevokeDialogId] = useState<number | null>(null);

  useEffect(() => {
    if (userId) {
      dispatch(getUserAuthoritiesByUser(userId));
    }
  }, [userId]);

  const getStatusBadge = (userAuthority: any) => {
    const now = new Date();
    const expiresAt = userAuthority.expiresAt ? new Date(userAuthority.expiresAt) : null;

    if (!userAuthority.isActive || userAuthority.revokedDate) {
      return (
        <Badge color="danger">
          <Translate contentKey="authorization.userAuthority.revoked">Revoked</Translate>
        </Badge>
      );
    }

    if (expiresAt && expiresAt < now) {
      return (
        <Badge color="warning">
          <Translate contentKey="authorization.userAuthority.expired">Expired</Translate>
        </Badge>
      );
    }

    return (
      <Badge color="success">
        <Translate contentKey="authorization.userAuthority.active">Active</Translate>
      </Badge>
    );
  };

  const handleAssignSuccess = () => {
    setShowAssignDialog(false);
    dispatch(getUserAuthoritiesByUser(userId));
  };

  const handleRevokeSuccess = () => {
    setRevokeDialogId(null);
    dispatch(getUserAuthoritiesByUser(userId));
  };

  return (
    <div className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3>
          <Translate contentKey="authorization.userAuthority.home.title">User Authorities</Translate>
        </h3>
        <Button color="primary" size="sm" onClick={() => setShowAssignDialog(true)}>
          <FontAwesomeIcon icon="plus" /> <Translate contentKey="authorization.userAuthority.home.assignLabel">Assign Authority</Translate>
        </Button>
      </div>

      <div className="table-responsive">
        {userAuthorities && userAuthorities.length > 0 ? (
          <Table responsive size="sm">
            <thead>
              <tr>
                <th>
                  <Translate contentKey="authorization.authority.name">Authority</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.userAuthority.assignedBy">Assigned By</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.userAuthority.assignedDate">Assigned Date</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.userAuthority.expiresAt">Expires At</Translate>
                </th>
                <th>
                  <Translate contentKey="authorization.userAuthority.isActive">Status</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {userAuthorities.map((userAuthority, i) => (
                <tr key={`entity-${i}`}>
                  <td>
                    <strong>{userAuthority.authority?.name}</strong>
                    {userAuthority.authority?.description && (
                      <>
                        <br />
                        <small className="text-muted">{userAuthority.authority.description}</small>
                      </>
                    )}
                  </td>
                  <td>{userAuthority.assignedBy}</td>
                  <td>
                    {userAuthority.assignedDate && <TextFormat value={userAuthority.assignedDate} type="date" format={APP_DATE_FORMAT} />}
                  </td>
                  <td>
                    {userAuthority.expiresAt ? (
                      <TextFormat value={userAuthority.expiresAt} type="date" format={APP_DATE_FORMAT} />
                    ) : (
                      <Badge color="secondary">Permanent</Badge>
                    )}
                  </td>
                  <td>{getStatusBadge(userAuthority)}</td>
                  <td className="text-end">
                    {userAuthority.isActive && !userAuthority.revokedDate && (
                      <Button
                        color="danger"
                        size="sm"
                        onClick={() => setRevokeDialogId(userAuthority.id)}
                        disabled={userAuthority.authority?.isSystem}
                      >
                        <FontAwesomeIcon icon="ban" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="authorization.userAuthority.revokeRole">Revoke</Translate>
                        </span>
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="authorization.userAuthority.home.notFound">No assigned authorities found</Translate>
            </div>
          )
        )}
      </div>

      {showAssignDialog && (
        <AssignAuthorityDialog
          userId={userId}
          isOpen={showAssignDialog}
          onClose={() => setShowAssignDialog(false)}
          onSuccess={handleAssignSuccess}
        />
      )}

      {revokeDialogId && (
        <RevokeAuthorityDialog
          userAuthorityId={revokeDialogId}
          isOpen={!!revokeDialogId}
          onClose={() => setRevokeDialogId(null)}
          onSuccess={handleRevokeSuccess}
        />
      )}
    </div>
  );
};

export default UserAuthorityList;
