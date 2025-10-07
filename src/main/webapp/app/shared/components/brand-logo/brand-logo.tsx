import React from 'react';
import './brand-logo.scss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope, faGlobe } from '@fortawesome/free-solid-svg-icons';
import { NavLink } from 'reactstrap';
import { Link } from 'react-router';

export default function BrandLogo() {
  return (
    <div className="brand-logo">
      <span className="tyse-logo rounded" />
      <span className="icons-array">
        <NavLink tag={Link} to="mailto:ejortegon@tecnologiayservicioselectorales.com" className="d-flex align-items-center">
          <FontAwesomeIcon icon={faEnvelope} />
        </NavLink>
        <NavLink tag={Link} to="https://www.tecnologiayservicioselectorales.com" className="d-flex align-items-center">
          <FontAwesomeIcon icon={faGlobe} />
        </NavLink>
      </span>
    </div>
  );
}
