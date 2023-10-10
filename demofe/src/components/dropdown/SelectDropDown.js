import React from 'react'
import './SelectDropDown.css'
import { useState } from 'react'
import { AiFillCaretUp, AiFillCaretDown, AiFillDelete } from 'react-icons/ai'
const SelectDropDown = ({ value, data, handleOnClick, handleClickDelete }) => {
    const [isActive, setIsActive] = useState(false)


    return (
        <div className="dropdown">

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