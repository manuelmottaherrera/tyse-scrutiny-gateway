import React from 'react';
import { Translate, ValidatedField, translate } from 'react-jhipster';
import { Alert, Button, Col, Form, Modal, ModalBody, ModalFooter, ModalHeader, Row } from 'reactstrap';
import { Link } from 'react-router-dom';
import { type FieldError, useForm } from 'react-hook-form';

import useRecaptcha from 'app/shared/custom-hooks/recaptcha/useRecaptcha';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const { verifyRecaptcha, isLoadingRecaptcha, errorRecaptcha } = useRecaptcha('login');
  const login = async ({ username, password, rememberMe }) => {
    const validRecaptcha: boolean = await verifyRecaptcha();
    if (errorRecaptcha) {
      setError('root.recaptcha', { type: 'manual', message: errorRecaptcha || 'invalid recaptcha' });
    }
    if (validRecaptcha === true) {
      props.handleLogin(username, password, rememberMe);
    }
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields, isSubmitting },
    setError,
  } = useForm({ mode: 'onTouched' });

  const { loginError, handleClose } = props;

  const handleLoginSubmit = e => {
    handleSubmit(login)(e);
  };

  return (
    <Modal isOpen={props.showModal} toggle={handleClose} backdrop="static" id="login-page" autoFocus={false}>
      <Form onSubmit={handleLoginSubmit}>
        <ModalHeader id="login-title" data-cy="loginTitle" toggle={handleClose}>
          <Translate contentKey="login.title">Sign in</Translate>
        </ModalHeader>
        <ModalBody>
          <Row>
            <Col md="12">
              {loginError ? (
                <Alert color="danger" data-cy="loginError" fade={false}>
                  <Translate contentKey="login.messages.error.authentication">
                    <strong>Failed to sign in!</strong> Please check your credentials and try again.
                  </Translate>
                </Alert>
              ) : null}
            </Col>
            <Col md="12">
              {errorRecaptcha ? (
                <Alert color="danger" data-cy="recaptchaError" fade={false}>
                  <Translate contentKey="login.messages.error.recaptcha">
                    <strong>reCAPTCHA verification failed!</strong> Please try again.
                  </Translate>
                  {errorRecaptcha}
                </Alert>
              ) : null}
            </Col>
            <Col md="12">
              <ValidatedField
                name="username"
                label={translate('global.form.username.label')}
                placeholder={translate('global.form.username.placeholder')}
                required
                autoFocus
                data-cy="username"
                validate={{ required: 'Username cannot be empty!' }}
                register={register}
                error={errors.username as FieldError}
                isTouched={touchedFields.username}
              />
              <ValidatedField
                name="password"
                type="password"
                label={translate('login.form.password')}
                placeholder={translate('login.form.password.placeholder')}
                required
                data-cy="password"
                validate={{ required: 'Password cannot be empty!' }}
                register={register}
                error={errors.password as FieldError}
                isTouched={touchedFields.password}
              />
              <ValidatedField
                name="rememberMe"
                type="checkbox"
                check
                label={translate('login.form.rememberme')}
                value={true}
                register={register}
              />
            </Col>
          </Row>
          <div className="mt-1">&nbsp;</div>
          <Alert color="warning" fade={false}>
            <Link to="/account/reset/request" data-cy="forgetYourPasswordSelector">
              <Translate contentKey="login.password.forgot">Did you forget your password?</Translate>
            </Link>
          </Alert>
          <Alert color="warning" fade={false}>
            <span>
              <Translate contentKey="global.messages.info.register.noaccount">You don&apos;t have an account yet?</Translate>
            </span>{' '}
            <Link to="/account/register">
              <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
            </Link>
          </Alert>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={handleClose} tabIndex={1}>
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>{' '}
          <Button color="primary" type="submit" data-cy="submit" disabled={isSubmitting || isLoadingRecaptcha}>
            {isSubmitting || isLoadingRecaptcha ? (
              <Translate contentKey="login.form.button.loading">Checking...</Translate>
            ) : (
              <Translate contentKey="login.form.button">Sign in</Translate>
            )}
          </Button>
        </ModalFooter>
      </Form>
    </Modal>
  );
};

export default LoginModal;
