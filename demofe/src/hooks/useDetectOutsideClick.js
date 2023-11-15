import { useEffect, useState } from 'react';
// use for dropdown
const useDetectOutsideClick = (ref, initialState, excludeClass) => {
  const [isActive, setIsActive] = useState(initialState);

  // isActive changing will trigger this function: 
  useEffect(() => {
    // * click everywhere trigger this func:
    const onClick = (event) => {


      if (ref.current !== null && !ref.current.contains(event.target) && excludeClass != event.target.classList[0]) {
        setIsActive(false);
      }
    };
    if (isActive) {
      window.addEventListener('mousedown', onClick);
    }

    return () => {
      window.removeEventListener('mousedown', onClick);
    };

  }, [isActive, ref]);

  return [isActive, setIsActive];
};
export default useDetectOutsideClick;


