import ChangeView from "./ChangeView";
import { MapContainer, TileLayer } from "react-leaflet";
function Map( props ) {
    const {  styles, center, zoom } = props
    return (

        <MapContainer style={styles} center={center} zoom={zoom} >
            <ChangeView center={center} zoom={zoom} />
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"

                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {props.children}
        </MapContainer>

    );
}

export default Map