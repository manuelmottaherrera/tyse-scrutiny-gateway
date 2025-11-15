import React, { useEffect, useState } from 'react';
import { Button, Col, Row } from 'reactstrap';
import { Storage, Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { useNavigate, useSearchParams } from 'react-router-dom';

import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import { handlePasswordResetFinish, reset } from '../password-reset.reducer';

export const PasswordResetFinishPage = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [password, setPassword] = useState('');

  const key = searchParams.get('key');

  useEffect(() => {
    const lang = searchParams.get('lang');

    // Set locale from URL parameter if provided (e.g., from creation email)
    if (lang && (lang === 'es' || lang === 'en')) {
      Storage.session.set('locale', lang);
      dispatch(setLocale(lang));
    }

    return () => {
      dispatch(reset());
    };
  }, []); // Dependencies intentionally empty to run only on mount

  const successMessage = useAppSelector(state => state.passwordReset.successMessage);

  useEffect(() => {
    if (successMessage) {
      // Delay navigation to allow toast notification to render
      const timer = setTimeout(() => {
        navigate('/');
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [successMessage, navigate]);

  const handleValidSubmit = ({ newPassword }) => dispatch(handlePasswordResetFinish({ key, newPassword }));

  const updatePassword = event => setPassword(event.target.value);

  const getResetForm = () => {
    return (
      <ValidatedForm onSubmit={handleValidSubmit}>
        <ValidatedField
          name="newPassword"
          label={translate('global.form.newpassword.label')}
          placeholder={translate('global.form.newpassword.placeholder')}
          type="password"
          validate={{
            required: { value: true, message: translate('global.messages.validate.newpassword.required') },
            minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
            maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
          }}
          onChange={updatePassword}
          data-cy="resetPassword"
        />
        <PasswordStrengthBar password={password} />
        <ValidatedField
          name="confirmPassword"
          label={translate('global.form.confirmpassword.label')}
          placeholder={translate('global.form.confirmpassword.placeholder')}
          type="password"
          validate={{
            required: { value: true, message: translate('global.messages.validate.confirmpassword.required') },
            minLength: { value: 4, message: translate('global.messages.validate.confirmpassword.minlength') },
            maxLength: { value: 50, message: translate('global.messages.validate.confirmpassword.maxlength') },
            validate: v => v === password || translate('global.messages.error.dontmatch'),
          }}
          data-cy="confirmResetPassword"
        />
        <Button color="success" type="submit" data-cy="submit">
          <Translate contentKey="reset.finish.form.button">Validate new password</Translate>
        </Button>
      </ValidatedForm>
    );
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="4">
          <h1>
            <Translate contentKey="reset.finish.title">Reset password</Translate>
          </h1>
          <div>{key ? getResetForm() : null}</div>
        </Col>
      </Row>
    </div>
  );
};

export default PasswordResetFinishPage;
