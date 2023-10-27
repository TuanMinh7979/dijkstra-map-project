import React from 'react'
import './SelectDropDown.css'

import { AiFillCaretUp, AiFillCaretDown, AiFillDelete } from 'react-icons/ai'
import useDetectOutsideClick from '../../hooks/useDetectOutsideClick'
import { useRef } from 'react'
const SelectDropDown = ({ value, data, handleOnClick, handleClickDelete, classNameProp }) => {
    const inputRef = useRef()
    const [isActive, setIsActive] = useDetectOutsideClick(inputRef, false, '')


    return (
        <div ref={inputRef} className={`dropdown ${classNameProp ? classNameProp : ''}`}>

            <div className="dropdown-btn" onClick={() => setIsActive(!isActive)}>
                {value}
                {isActive ? <AiFillCaretUp /> : <AiFillCaretDown />}
            </div>

            {isActive && <div className="dropdown-content">

                {data.length > 0 && data.map(el =>
                    <div className="dropdown-item" onClick={() => {
                        handleOnClick(el)
                        setIsActive(false)
                    }}>

                        <span>{el}</span>

                        <AiFillDelete onClick={(event) => {
                            event.stopPropagation()
                            setIsActive(false)
                            handleClickDelete(el)
                        }
                        } />

                    </div>)

                }


            </div>}


        </div>

    )
}

export default SelectDropDown