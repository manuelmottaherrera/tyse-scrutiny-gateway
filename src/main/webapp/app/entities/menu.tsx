import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      <MenuItem icon="map-marked-alt" to="/divipol">
        <Translate contentKey="global.menu.entities.divipol">DIVIPOL</Translate>
      </MenuItem>
      <MenuItem icon="file-pdf" to="/e14">
        <Translate contentKey="global.menu.entities.e14">Formularios E14</Translate>
      </MenuItem>
      <MenuItem icon="exclamation-triangle" to="/anomalies">
        <Translate contentKey="global.menu.entities.anomalies">Anomalías</Translate>
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
