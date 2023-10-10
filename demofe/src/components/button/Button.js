import React from 'react'

const Button = (props) => {
    const { label, className, disabled, handleClick } = props
    return (
        <>
            <button className={`${className} ${disabled}`}
                onClick={handleClick}
                disabled={disabled}
            >
                {label}</button>
        </>
    )
}


export default Button