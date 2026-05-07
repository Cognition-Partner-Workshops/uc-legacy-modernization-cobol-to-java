import React, { useState } from 'react';

const terminalStyle = {
  background: '#000',
  color: '#33ff33',
  fontFamily: '"IBM Plex Mono", "Courier New", monospace',
  fontSize: '14px',
  lineHeight: '1.4',
  padding: '16px',
  borderRadius: '8px',
  border: '2px solid #333',
  whiteSpace: 'pre',
  overflow: 'auto',
  minHeight: '480px',
  position: 'relative',
};

const headerStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  marginBottom: '24px',
};

const titleStyle = { fontSize: '24px', fontWeight: 700, color: '#1a237e' };

const tabBar = {
  display: 'flex',
  gap: '0',
  marginBottom: '16px',
};

const tabActive = {
  padding: '8px 20px',
  background: '#000',
  color: '#33ff33',
  border: '1px solid #333',
  borderBottom: 'none',
  cursor: 'pointer',
  fontSize: '13px',
  fontFamily: 'monospace',
  fontWeight: 700,
};

const tabInactive = {
  padding: '8px 20px',
  background: '#1a1a1a',
  color: '#666',
  border: '1px solid #333',
  borderBottom: '1px solid #333',
  cursor: 'pointer',
  fontSize: '13px',
  fontFamily: 'monospace',
};

const blue = { color: '#5599ff' };
const yellow = { color: '#ffff55' };
const turq = { color: '#55ffff' };
const white = { color: '#ffffff' };
const red = { color: '#ff5555' };
const amber = { color: '#ffaa00' };
const underline = { textDecoration: 'underline' };

function pad(str, len) {
  return (str || '').padEnd(len).slice(0, len);
}

function CardDetailScreen() {
  return (
    <div style={terminalStyle}>
      <div>
        <span style={blue}>Tran:</span>
        <span style={blue}> CC03</span>
        <span>{'          '}</span>
        <span style={yellow}>{pad('CardDemo - Credit Card Detail View', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Date:</span>
        <span style={blue}> 04/24/26</span>
      </div>
      <div>
        <span style={blue}>Prog:</span>
        <span style={blue}> COCRDSLC</span>
        <span>{'       '}</span>
        <span style={yellow}>{pad('', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Time:</span>
        <span style={blue}> 10:30:00</span>
      </div>
      <div>{'\n'}</div>
      <div style={{ textAlign: 'center' }}>
        <span style={white}>View Credit Card Detail</span>
      </div>
      <div>{'\n\n'}</div>
      <div>
        <span>{'                      '}</span>
        <span style={turq}>Account Number    : </span>
        <span style={{ ...white, ...underline }}>00000000050</span>
      </div>
      <div>
        <span>{'                      '}</span>
        <span style={turq}>Card Number       : </span>
        <span style={{ ...white, ...underline }}>0500024453765740</span>
      </div>
      <div>{'\n\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Name on card      : </span>
        <span style={{ ...white, ...underline }}>{pad('Aniya Von', 50)}</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Card Active Y/N   : </span>
        <span style={{ ...white, ...underline }}>Y</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Expiry Date       : </span>
        <span style={{ ...white, ...underline }}>03</span>
        <span>/</span>
        <span style={{ ...white, ...underline }}>2023</span>
      </div>
      <div>{'\n\n\n\n'}</div>
      <div style={{ position: 'absolute', bottom: '32px', left: '16px' }}>
        <span style={yellow}>ENTER=Search Cards  F3=Exit</span>
      </div>
    </div>
  );
}

function CardUpdateScreen() {
  return (
    <div style={terminalStyle}>
      <div>
        <span style={blue}>Tran:</span>
        <span style={blue}> CC04</span>
        <span>{'          '}</span>
        <span style={yellow}>{pad('CardDemo - Credit Card Update', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Date:</span>
        <span style={blue}> 04/24/26</span>
      </div>
      <div>
        <span style={blue}>Prog:</span>
        <span style={blue}> COCRDUPC</span>
        <span>{'       '}</span>
        <span style={yellow}>{pad('', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Time:</span>
        <span style={blue}> 10:32:00</span>
      </div>
      <div>{'\n'}</div>
      <div style={{ textAlign: 'center' }}>
        <span style={white}>Update Credit Card Details</span>
      </div>
      <div>{'\n\n'}</div>
      <div>
        <span>{'                      '}</span>
        <span style={turq}>Account Number    : </span>
        <span style={{ ...white, ...underline }}>00000000050</span>
      </div>
      <div>
        <span>{'                      '}</span>
        <span style={turq}>Card Number       : </span>
        <span style={{ ...amber, ...underline }}>0500024453765740</span>
      </div>
      <div>{'\n\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Name on card      : </span>
        <span style={{ ...amber, ...underline }}>{pad('Aniya Von', 50)}</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Card Active Y/N   : </span>
        <span style={{ ...amber, ...underline }}>Y</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span>{'   '}</span>
        <span style={turq}>Expiry Date       : </span>
        <span style={{ ...amber, ...underline }}>03</span>
        <span>/</span>
        <span style={{ ...amber, ...underline }}>2023</span>
      </div>
      <div>{'\n\n\n\n'}</div>
      <div style={{ position: 'absolute', bottom: '32px', left: '16px' }}>
        <span style={yellow}>ENTER=Update  F3=Exit  F5=Card Detail</span>
      </div>
    </div>
  );
}

function CardListScreen() {
  return (
    <div style={terminalStyle}>
      <div>
        <span style={blue}>Tran:</span>
        <span style={blue}> CC02</span>
        <span>{'          '}</span>
        <span style={yellow}>{pad('CardDemo - Credit Card List', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Date:</span>
        <span style={blue}> 04/24/26</span>
      </div>
      <div>
        <span style={blue}>Prog:</span>
        <span style={blue}> COCRDLIC</span>
        <span>{'       '}</span>
        <span style={yellow}>{pad('', 40)}</span>
        <span>{'    '}</span>
        <span style={blue}>Time:</span>
        <span style={blue}> 10:28:00</span>
      </div>
      <div>{'\n'}</div>
      <div style={{ textAlign: 'center' }}>
        <span style={white}>List Credit Cards</span>
        <span>{'                                   '}</span>
        <span style={white}>Page 001</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span>{'                     '}</span>
        <span style={turq}>Account Number    : </span>
        <span style={{ ...white, ...underline }}>{pad('', 11)}</span>
      </div>
      <div>
        <span>{'                     '}</span>
        <span style={turq}>Credit Card Number: </span>
        <span style={{ ...white, ...underline }}>{pad('', 16)}</span>
      </div>
      <div>{'\n'}</div>
      <div>
        <span style={white}>{'         Select    Account Number    Card Number      Active '}</span>
      </div>
      <div>
        <span style={white}>{'         ------    ---------------  ---------------  --------'}</span>
      </div>
      <div>
        <span>{'         '}</span>
        <span style={{ ...white, ...underline }}>{'_'}</span>
        <span>{'         '}</span>
        <span style={white}>00000000050</span>
        <span>{'    '}</span>
        <span style={white}>0500024453765740</span>
        <span>{'  '}</span>
        <span style={white}>Y</span>
      </div>
      <div>
        <span>{'         '}</span>
        <span style={{ ...white, ...underline }}>{'_'}</span>
        <span>{'         '}</span>
        <span style={white}>00000000027</span>
        <span>{'    '}</span>
        <span style={white}>0683586198171516</span>
        <span>{'  '}</span>
        <span style={white}>Y</span>
      </div>
      <div>
        <span>{'         '}</span>
        <span style={{ ...white, ...underline }}>{'_'}</span>
        <span>{'         '}</span>
        <span style={white}>00000000002</span>
        <span>{'    '}</span>
        <span style={white}>0923877193247330</span>
        <span>{'  '}</span>
        <span style={white}>Y</span>
      </div>
      <div>
        <span>{'         '}</span>
        <span style={{ ...white, ...underline }}>{'_'}</span>
        <span>{'         '}</span>
        <span style={white}>00000000020</span>
        <span>{'    '}</span>
        <span style={white}>0927987108636232</span>
        <span>{'  '}</span>
        <span style={white}>Y</span>
      </div>
      <div>
        <span>{'         '}</span>
        <span style={{ ...white, ...underline }}>{'_'}</span>
        <span>{'         '}</span>
        <span style={white}>00000000012</span>
        <span>{'    '}</span>
        <span style={white}>0982496213629795</span>
        <span>{'  '}</span>
        <span style={white}>Y</span>
      </div>
      <div>{'\n\n\n\n'}</div>
      <div style={{ position: 'absolute', bottom: '32px', left: '16px' }}>
        <span style={yellow}>S=Select  ENTER=Search  F7=Back  F8=Next  F3=Exit</span>
      </div>
    </div>
  );
}

function GreenScreenPreview() {
  const [tab, setTab] = useState('list');

  return (
    <div>
      <div style={headerStyle}>
        <div>
          <div style={titleStyle}>Green Screen Preview</div>
          <div style={{ color: '#666', fontSize: '14px' }}>
            Original IBM 3270 terminal layouts from the BMS map definitions
          </div>
        </div>
      </div>

      <div style={tabBar}>
        <button
          style={tab === 'list' ? tabActive : tabInactive}
          onClick={() => setTab('list')}
        >
          COCRDLI — Card List
        </button>
        <button
          style={tab === 'detail' ? tabActive : tabInactive}
          onClick={() => setTab('detail')}
        >
          COCRDSL — Card Detail
        </button>
        <button
          style={tab === 'update' ? tabActive : tabInactive}
          onClick={() => setTab('update')}
        >
          COCRDUP — Card Update
        </button>
      </div>

      {tab === 'list' && <CardListScreen />}
      {tab === 'detail' && <CardDetailScreen />}
      {tab === 'update' && <CardUpdateScreen />}

      <div style={{ marginTop: '16px', fontSize: '13px', color: '#999' }}>
        <strong>Note:</strong> These screens are rendered using the exact field positions (POS coordinates)
        from the BMS map definitions. On a real mainframe, these would appear on an IBM 3270 terminal
        with 24 rows x 80 columns. Colors: <span style={{ color: '#33ff33' }}>Green</span> = default,{' '}
        <span style={{ color: '#5599ff' }}>Blue</span> = system fields,{' '}
        <span style={{ color: '#ffff55' }}>Yellow</span> = titles/function keys,{' '}
        <span style={{ color: '#55ffff' }}>Turquoise</span> = labels,{' '}
        <span style={{ color: '#ffaa00' }}>Amber</span> = editable fields.
      </div>
    </div>
  );
}

export default GreenScreenPreview;
