import React, { useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Alert, Col, Row } from 'reactstrap';
import { Storage, Translate } from 'react-jhipster';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import { activateAction, reset } from './activate.reducer';

const successAlert = (
  <Alert color="success">
    <Translate contentKey="activate.messages.success">
      <strong>Your user account has been activated.</strong> Please
    </Translate>
    <Link to="/login" className="alert-link">
      <Translate contentKey="global.messages.info.authenticated.link">sign in</Translate>
    </Link>
    .
  </Alert>
);

const failureAlert = (
  <Alert color="danger">
    <Translate contentKey="activate.messages.error">
      <strong>Your user could not be activated.</strong> Please use the registration form to sign up.
    </Translate>
  </Alert>
);

export const ActivatePage = () => {
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const key = searchParams.get('key');
    const lang = searchParams.get('lang');

    // Set locale from URL parameter if provided (e.g., from activation email)
    if (lang && (lang === 'es' || lang === 'en')) {
      Storage.session.set('locale', lang);
      dispatch(setLocale(lang));
    }

    dispatch(activateAction(key));
    return () => {
      dispatch(reset());
    };
  }, []); // Dependencies intentionally empty to run only on mount

  const { activationSuccess, activationFailure } = useAppSelector(state => state.activate);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h1>
            <Translate contentKey="activate.title">Activation</Translate>
          </h1>
          {activationSuccess ? successAlert : undefined}
          {activationFailure ? failureAlert : undefined}
        </Col>
      </Row>
    </div>
  );
};

export default ActivatePage;
