import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Alert, Col, Row } from 'reactstrap';

import { useAppSelector } from 'app/config/store';
import BrandLogo from 'app/shared/components/brand-logo/brand-logo';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="3" className="pad">
        <BrandLogo />
      </Col>
      <Col md="9">
        <h1 className="display-4">
          <Translate contentKey="home.title">Welcome!</Translate>
        </h1>
        <p className="lead">
          <Translate contentKey="home.subtitle">This is your homepage</Translate>
        </p>
        {account?.login ? (
          <div>
            <Alert color="success" fade={false}>
              <Translate contentKey="home.logged.message" interpolate={{ username: account.login }}>
                You are logged in as user {account.login}.
              </Translate>
            </Alert>
          </div>
        ) : (
          <div>
            <Alert color="warning" fade={false}>
              <Translate contentKey="global.messages.info.register.noaccount">You do not have an account yet?</Translate>&nbsp;
              <Link to="/account/register" className="alert-link">
                <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
              </Link>
            </Alert>
          </div>
        )}
      </Col>
    </Row>
  );
};

export default Home;
