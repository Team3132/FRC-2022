import { r as react } from './common/index-04edb6a1.js';
import './common/_commonjsHelpers-8c19dec8.js';

function useEventListener(eventName, handler, element) {
    const savedHandler = react.useRef();
    react.useEffect(() => {
        const targetElement = element?.current || window;
        if (!(targetElement && targetElement.addEventListener)) {
            return;
        }
        if (savedHandler.current !== handler) {
            savedHandler.current = handler;
        }
        const eventListener = event => {
            if (!!savedHandler?.current) {
                savedHandler.current(event);
            }
        };
        targetElement.addEventListener(eventName, eventListener);
        return () => {
            targetElement.removeEventListener(eventName, eventListener);
        };
    }, [eventName, element, handler]);
}

function useLocalStorage(key, initialValue) {
    const readValue = () => {
        if (typeof window === 'undefined') {
            return initialValue;
        }
        try {
            const item = window.localStorage.getItem(key);
            return item ? parseJSON(item) : initialValue;
        }
        catch (error) {
            console.warn(`Error reading localStorage key “${key}”:`, error);
            return initialValue;
        }
    };
    const [storedValue, setStoredValue] = react.useState(readValue);
    const setValue = value => {
        if (typeof window == 'undefined') {
            console.warn(`Tried setting localStorage key “${key}” even though environment is not a client`);
        }
        try {
            const newValue = value instanceof Function ? value(storedValue) : value;
            window.localStorage.setItem(key, JSON.stringify(newValue));
            setStoredValue(newValue);
            window.dispatchEvent(new Event('local-storage'));
        }
        catch (error) {
            console.warn(`Error setting localStorage key “${key}”:`, error);
        }
    };
    react.useEffect(() => {
        setStoredValue(readValue());
    }, []);
    const handleStorageChange = () => {
        setStoredValue(readValue());
    };
    useEventListener('storage', handleStorageChange);
    useEventListener('local-storage', handleStorageChange);
    return [storedValue, setValue];
}
function parseJSON(value) {
    try {
        return value === 'undefined' ? undefined : JSON.parse(value ?? '');
    }
    catch (error) {
        console.log('parsing error on', { value });
        return undefined;
    }
}

function useDebounce(value, delay) {
    const [debouncedValue, setDebouncedValue] = react.useState(value);
    react.useEffect(() => {
        const timer = setTimeout(() => setDebouncedValue(value), delay || 500);
        return () => {
            clearTimeout(timer);
        };
    }, [value, delay]);
    return debouncedValue;
}

export { useDebounce, useLocalStorage };
