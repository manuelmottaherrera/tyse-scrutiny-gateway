import React, { useState, useRef, useEffect, useCallback } from 'react';
import { InputGroup, InputGroupText, Button } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { translate } from 'react-jhipster';
import './divipol-code-input.scss';

interface DivipolCodeInputProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: (value: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

const CODE_LENGTH = 9;
const EMPTY_CODE = '000000000';

/**
 * Input especial para código divipol de 9 caracteres con cursor terminal.
 * Formato: DD MMM ZZ PP (2+3+2+2 caracteres)
 * Las posiciones 0-6 son solo dígitos, las posiciones 7-8 (PP) son alfanuméricas.
 */
export const DivipolCodeInput: React.FC<DivipolCodeInputProps> = ({ value, onChange, onSearch, disabled = false, placeholder }) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [cursorPosition, setCursorPosition] = useState(0);
  const [isFocused, setIsFocused] = useState(false);

  // Valor mostrado: reemplaza caracteres ingresados sobre los ceros
  const displayValue = value.padEnd(CODE_LENGTH, '0').substring(0, CODE_LENGTH);

  // Función para procesar texto pegado
  const processPastedText = useCallback(
    (text: string) => {
      const pastedText = text.toUpperCase();
      let result = '';
      for (let i = 0; i < pastedText.length && result.length < CODE_LENGTH; i++) {
        const char = pastedText[i];
        const targetPos = result.length;
        const isDigit = /^\d$/.test(char);
        const isLetter = /^[A-Z]$/.test(char);

        if (targetPos < 7) {
          if (isDigit) result += char;
        } else {
          if (isDigit || isLetter) result += char;
        }
      }

      if (result) {
        onChange(result);
        setCursorPosition(Math.min(result.length, CODE_LENGTH - 1));
      }
    },
    [onChange],
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      const key = e.key;

      // Ctrl+V o Cmd+V: pegar desde portapapeles
      if ((e.ctrlKey || e.metaKey) && key === 'v') {
        e.preventDefault();
        navigator.clipboard
          .readText()
          .then(processPastedText)
          .catch(() => {});
        return;
      }

      // Enter: ejecutar búsqueda (permite valor vacío)
      if (key === 'Enter') {
        e.preventDefault();
        onSearch(value);
        return;
      }

      // Flechas: mover cursor (por todos los 9 dígitos)
      if (key === 'ArrowLeft') {
        e.preventDefault();
        setCursorPosition(prev => Math.max(0, prev - 1));
        return;
      }

      if (key === 'ArrowRight') {
        e.preventDefault();
        setCursorPosition(prev => Math.min(CODE_LENGTH - 1, prev + 1));
        return;
      }

      // Home: ir al inicio
      if (key === 'Home') {
        e.preventDefault();
        setCursorPosition(0);
        return;
      }

      // End: ir al final
      if (key === 'End') {
        e.preventDefault();
        setCursorPosition(CODE_LENGTH - 1);
        return;
      }

      // Backspace: eliminar carácter antes del cursor o mover cursor atrás
      if (key === 'Backspace') {
        e.preventDefault();
        if (cursorPosition > 0) {
          if (cursorPosition <= value.length) {
            const newValue = value.slice(0, cursorPosition - 1) + value.slice(cursorPosition);
            onChange(newValue);
          }
          setCursorPosition(prev => prev - 1);
        }
        return;
      }

      // Delete: eliminar carácter en el cursor
      if (key === 'Delete') {
        e.preventDefault();
        if (cursorPosition < value.length) {
          const newValue = value.slice(0, cursorPosition) + value.slice(cursorPosition + 1);
          onChange(newValue);
        }
        return;
      }

      // Validar carácter según posición:
      // - Posiciones 0-6: solo dígitos
      // - Posiciones 7-8: alfanumérico (código de puesto)
      const isDigit = /^\d$/.test(key);
      const isLetter = /^[a-zA-Z]$/.test(key);
      const isValidChar = cursorPosition < 7 ? isDigit : isDigit || isLetter;

      if (isValidChar) {
        e.preventDefault();
        // Convertir letras a mayúsculas
        const char = isLetter ? key.toUpperCase() : key;
        let newValue: string;

        if (cursorPosition < value.length) {
          // Reemplazar carácter existente
          newValue = value.substring(0, cursorPosition) + char + value.substring(cursorPosition + 1);
        } else {
          // Extender el valor hasta la posición del cursor
          newValue = value.padEnd(cursorPosition, '0') + char;
        }

        // Limitar a CODE_LENGTH
        newValue = newValue.substring(0, CODE_LENGTH);
        onChange(newValue);
        // Mover cursor a la siguiente posición
        setCursorPosition(prev => Math.min(prev + 1, CODE_LENGTH - 1));
        return;
      }

      // Ignorar otras teclas (excepto Tab y Escape)
      if (!['Tab', 'Escape'].includes(key)) {
        e.preventDefault();
      }
    },
    [value, cursorPosition, onChange, onSearch, processPastedText],
  );

  const handlePaste = useCallback(
    (e: React.ClipboardEvent<HTMLInputElement>) => {
      e.preventDefault();
      const text = e.clipboardData.getData('text');
      processPastedText(text);
    },
    [processPastedText],
  );

  const handlePasteClick = useCallback(() => {
    navigator.clipboard
      .readText()
      .then(processPastedText)
      .catch(() => {});
  }, [processPastedText]);

  const handleFocus = () => {
    setIsFocused(true);
  };

  const handleBlur = () => {
    setIsFocused(false);
  };

  // Formatea el código para mostrar con separadores visuales
  const formatDisplayCode = () => {
    const chars = displayValue.split('');
    return chars.map((char, index) => {
      const isTyped = index < value.length;
      const isCursor = isFocused && index === cursorPosition;

      // Agregar separadores visuales
      let separator = '';
      if (index === 2 || index === 5 || index === 7) {
        separator = ' ';
      }

      return (
        <React.Fragment key={index}>
          {separator && <span className="code-separator">{separator}</span>}
          <span className={`code-digit ${isTyped ? 'typed' : 'empty'} ${isCursor ? 'cursor' : ''}`}>{char}</span>
        </React.Fragment>
      );
    });
  };

  return (
    <InputGroup className="divipol-code-input">
      <InputGroupText>
        <FontAwesomeIcon icon="hashtag" />
      </InputGroupText>
      <div
        className={`code-display-wrapper ${isFocused ? 'focused' : ''} ${disabled ? 'disabled' : ''}`}
        onClick={() => inputRef.current?.focus()}
      >
        <div className="code-display">{formatDisplayCode()}</div>
        <input
          ref={inputRef}
          type="text"
          className="code-hidden-input"
          value={value}
          onChange={() => {}}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          onFocus={handleFocus}
          onBlur={handleBlur}
          disabled={disabled}
          placeholder={placeholder}
          aria-label="Código Divipol"
          autoComplete="off"
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            opacity: 0,
            background: 'transparent',
            border: 'none',
            zIndex: 1,
            cursor: 'text',
          }}
        />
      </div>
      <Button color="secondary" outline size="sm" onClick={handlePasteClick} disabled={disabled} className="paste-btn">
        <FontAwesomeIcon icon="paste" className="me-1" />
        {translate('divipol.search.pasteCode')}
      </Button>
      <InputGroupText className="code-format-hint">
        <small>DD-MMM-ZZ-PP</small>
      </InputGroupText>
    </InputGroup>
  );
};

export default DivipolCodeInput;
