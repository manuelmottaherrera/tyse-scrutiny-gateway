import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Alert, Col, Row } from 'reactstrap';

import { useAppSelector } from 'app/config/store';
import BrandLogo from 'app/shared/components/brand-logo/brand-logo';
import { DashboardGrid } from 'app/shared/components/dashboard/dashboard-grid';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="12">
        {account?.login ? (
          <div>
            <DashboardGrid />
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
